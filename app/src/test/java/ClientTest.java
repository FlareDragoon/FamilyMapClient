import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import Model.DataCache;
import Model.EventModel;
import Model.PersonModel;
import Request.GetAllFamilyEventsRequest;
import Request.GetFamilyRequest;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.GetAllFamilyEventsResult;
import Result.GetFamilyResult;
import Result.LoginResult;
import Result.RegisterResult;
import ServerClient.ServerProxy;

public class ClientTest {
    private final String host = "localhost";
    private final String port = "8080";
    ServerProxy proxy = new ServerProxy();
    RegisterRequest testRegisterRequest;
    GetFamilyRequest testFamilyRequest;
    GetAllFamilyEventsRequest testEventsRequest;

    RegisterResult testRegisterResult;
    GetFamilyResult testFamilyResult;
    GetAllFamilyEventsResult testEventsResult;

    public void prepareTests() {
        proxy.main(host, port);

        String username = UUID.randomUUID().toString().replace("-", "");

        testRegisterRequest = new RegisterRequest(username, "testPassword",
                "testEmail", "testFirstName", "testLastName", "m");

        testRegisterResult = proxy.register(testRegisterRequest);
        testFamilyRequest = new GetFamilyRequest(testRegisterResult.getAuthtoken());
        testEventsRequest = new GetAllFamilyEventsRequest(testRegisterResult.getAuthtoken());

        testFamilyResult = proxy.getPeople(testFamilyRequest);
        testEventsResult = proxy.getEvents(testEventsRequest);

        DataCache.getInstance().setInstance(testEventsResult.getData(), testFamilyResult.getData(),
                testRegisterResult.getAuthtoken(), testRegisterResult.getPersonID());
    }

    @Test
    public void RegisterTestPass() {
        ServerProxy proxy = new ServerProxy();
        proxy.main(host, port);

        String username = UUID.randomUUID().toString().replace("-", "");

        RegisterRequest request = new RegisterRequest(username, "testPassword",
                "testEmail", "testFirstName", "testLastName", "m");

        RegisterResult result = proxy.register(request);

        assertTrue(result.isSuccess());
    }

    @Test
    public void RegisterTestFail() {
        ServerProxy proxy = new ServerProxy();
        proxy.main(host, port);

        String username = UUID.randomUUID().toString().replace("-", "");

        RegisterRequest request1 = new RegisterRequest(username, "testPassword1",
                "testEmail1", "testFirstName1", "testLastName1", "m");
        RegisterRequest request2 = new RegisterRequest("testUser1", "testPassword1",
                "testEmail1", "testFirstName1", "testLastName1", "m");

        RegisterResult result = proxy.register(request2);
        assertFalse(result.isSuccess());
    }

    @Test
    public void LoginTestPass() {
        ServerProxy proxy = new ServerProxy();
        proxy.main(host, port);

        RegisterRequest rRequest = new RegisterRequest("testUser", "testPassword",
                "testEmail", "testFirstName", "testLastName", "m");

        RegisterResult rResult = proxy.register(rRequest);

        LoginRequest lRequest = new LoginRequest("testUser", "testPassword");

        LoginResult lResult = proxy.login(lRequest);

        assertTrue(lResult.isSuccess());
    }

    @Test
    public void LoginTestFail() {
        ServerProxy proxy = new ServerProxy();
        proxy.main(host, port);

        LoginRequest request = new LoginRequest("badUsername", "badPassword");

        LoginResult result = proxy.login(request);
        assertFalse(result.isSuccess());
    }

    @Test
    public void GetRelationshipsTest() {
        prepareTests();

        PersonModel user = DataCache.getInstance().getPersonByID(testRegisterResult.getPersonID());
        PersonModel father = DataCache.getInstance().getPersonByID(user.getFatherID());
        PersonModel mother = DataCache.getInstance().getPersonByID(user.getMotherID());

        assertTrue(DataCache.getInstance().getRelationship(user.getPersonID(),
                father.getPersonID()).equalsIgnoreCase("father"));
        assertTrue(DataCache.getInstance().getRelationship(father.getPersonID(),
                user.getPersonID()).equalsIgnoreCase("child"));
        assertTrue(DataCache.getInstance().getRelationship(user.getPersonID(),
                mother.getPersonID()).equalsIgnoreCase("mother"));
        assertTrue(DataCache.getInstance().getRelationship(mother.getPersonID(),
                user.getPersonID()).equalsIgnoreCase("child"));
        assertTrue(DataCache.getInstance().getRelationship(father.getPersonID(),
                mother.getPersonID()).equalsIgnoreCase("spouse"));

        DataCache.getInstance().clearInstance();
    }

    @Test
    public void filterEventsTest() {
        prepareTests();
        DataCache.getInstance().setShowPaternalEvents(false);
        HashMap<String, EventModel> filteredEvents = DataCache.getInstance().getFilteredEvents();
        for (Map.Entry eventEntry : filteredEvents.entrySet()) {
            EventModel event = (EventModel) eventEntry.getValue();
            assertFalse(DataCache.getInstance().getPaternalAnscestors().contains(event.getPersonID()));
        }

        DataCache.getInstance().setShowPaternalEvents(true);
        DataCache.getInstance().setShowMaternalEvents(false);
        filteredEvents = DataCache.getInstance().getFilteredEvents();
        for (Map.Entry eventEntry : filteredEvents.entrySet()) {
            EventModel event = (EventModel) eventEntry.getValue();
            assertFalse(DataCache.getInstance().getMaternalAnscestors().contains(event.getPersonID()));
        }

        DataCache.getInstance().setShowMaternalEvents(true);
        DataCache.getInstance().setShowMaleEvents(false);
        filteredEvents = DataCache.getInstance().getFilteredEvents();
        for (Map.Entry eventEntry : filteredEvents.entrySet()) {
            EventModel event = (EventModel) eventEntry.getValue();
            PersonModel person = DataCache.getInstance().getPersonByID(event.getPersonID());
            assertNotEquals("m", person.getGender());
        }

        DataCache.getInstance().setShowMaleEvents(true);
        DataCache.getInstance().setShowFemaleEvents(false);
        filteredEvents = DataCache.getInstance().getFilteredEvents();
        for (Map.Entry eventEntry : filteredEvents.entrySet()) {
            EventModel event = (EventModel) eventEntry.getValue();
            PersonModel person = DataCache.getInstance().getPersonByID(event.getPersonID());
            assertNotEquals("f", person.getGender());
        }

        DataCache.getInstance().clearInstance();
    }

    @Test
    public void organizeEventsTest() {
        prepareTests();
        PersonModel user = DataCache.getInstance().getPersonByID(testRegisterResult.getPersonID());
        List<EventModel> fatherEvents = DataCache.getInstance().getPersonEvents(user.getFatherID());

        assertTrue(fatherEvents.get(0).getEventType().equalsIgnoreCase("birth"));
        assertTrue(fatherEvents.get(fatherEvents.size() - 1).getEventType().equalsIgnoreCase("death"));
        assertTrue(fatherEvents.get(0).getYear() < fatherEvents.get(1).getYear());

        DataCache.getInstance().clearInstance();
    }

    @Test
    public void searchPeopleTest() {
        prepareTests();
        PersonModel user = DataCache.getInstance().getPersonByID(testRegisterResult.getPersonID());
        PersonModel father = DataCache.getInstance().getPersonByID(testRegisterResult.getPersonID());

        String query = user.getFirstName();
        List<PersonModel> searchResults = DataCache.getInstance().searchForPeople(query);

        if (user.getFirstName().equals(father.getFirstName())) {
            assertTrue(searchResults.contains(user));
            assertTrue(searchResults.contains(father));
        } else {
            assertTrue(searchResults.contains(user));
            assertFalse(searchResults.contains(father));
        }


        query = father.getLastName();
        searchResults = DataCache.getInstance().searchForPeople(query);

        if (user.getFirstName().equals(father.getFirstName())) {
            assertTrue(searchResults.contains(user));
            assertTrue(searchResults.contains(father));
        } else {
            assertTrue(searchResults.contains(father));
            assertFalse(searchResults.contains(user));
        }

        DataCache.getInstance().clearInstance();
    }

    @Test
    public void searchEventsTest() {
        prepareTests();
        PersonModel user = DataCache.getInstance().getPersonByID(testRegisterResult.getPersonID());
        PersonModel father = DataCache.getInstance().getPersonByID(user.getPersonID());
        List<EventModel> events = DataCache.getInstance().getPersonEvents(father.getPersonID());

        String query = events.get(0).getEventType();
        List<EventModel> searchResults = DataCache.getInstance().searchForEvents(query);
        assertTrue(searchResults.contains(events.get(0)));
        if (events.size() > 1) {
            assertFalse(searchResults.contains(events.get(1)));
        }

        query = events.get(0).getCity();
        searchResults = DataCache.getInstance().searchForEvents(query);
        assertTrue(searchResults.contains(events.get(0)));
        if (events.size() > 1) {
            assertFalse(searchResults.contains(events.get(1)));
        }
        query = events.get(0).getCountry();
        searchResults = DataCache.getInstance().searchForEvents(query);
        assertTrue(searchResults.contains(events.get(0)));
        if (events.size() > 1) {
            assertFalse(searchResults.contains(events.get(1)));
        }
        query = events.get(0).getYear().toString();
        searchResults = DataCache.getInstance().searchForEvents(query);
        assertTrue(searchResults.contains(events.get(0)));
        if (events.size() > 1) {
            assertFalse(searchResults.contains(events.get(1)));
        }

        DataCache.getInstance().clearInstance();
    }

}
