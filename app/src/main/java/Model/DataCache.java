package Model;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DataCache {
    private static String authtoken;
    private static String rootID;

    private static HashMap<String, PersonModel> people;
    private static HashMap<String, EventModel> events;

    private static HashMap<String, List<EventModel>> personEvents;
    private static HashMap<String, List<PersonModel>> immediateFamily;

    private static List<String> paternalAncestors;
    private static List<String> maternalAncestors;

    private static Set<String> maleAncestors;
    private static Set<String> femaleAncestors;

    private static HashMap<String, List<String>> childrenOfPerson;

    private static final Float[] COLOR_OPTIONS = {
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_YELLOW
    };
    private static HashMap<String, Float> markerColors;

    private static HashMap<String, EventModel> filteredEvents;

    private static boolean showSpouseLine;
    private static boolean showFamilyTreeLines;
    private static boolean showLifeStoryLine;

    private static boolean showPaternalEvents;
    private static boolean showMaternalEvents;
    private static boolean showMaleEvents;
    private static boolean showFemaleEvents;

    private static DataCache instance;

    public DataCache(EventModel[] events, PersonModel[] persons,
                     String authtoken, String rootID) {
        this.authtoken = authtoken;
        this.rootID = rootID;

        this.showSpouseLine = true;
        this.showFamilyTreeLines = true;
        this.showLifeStoryLine = true;

        this.showPaternalEvents = true;
        this.showMaternalEvents = true;
        this.showMaleEvents = true;
        this.showFemaleEvents = true;

        this.people = setFamilyMap(persons);
        this.events = setEventsMap(events);

        this.personEvents = setPersonEvents(events, persons);

        this.paternalAncestors = setPaternalAncestors(getPersonByID(rootID).getFatherID());
        this.maternalAncestors = setPaternalAncestors(getPersonByID(rootID).getMotherID());

        this.maleAncestors = setMaleAncestors(persons);
        this.femaleAncestors = setFemaleAncestors(persons);

        this.childrenOfPerson = setChildrenOfPerson(persons);
        this.immediateFamily = setImmediateFamily(persons);

        this.markerColors = setMarkerColors(events);

        this.filteredEvents = filterEvents();
    }

    public List<PersonModel> searchForPeople(String searchQuery) {
        List<PersonModel> output = new ArrayList<>();
        for (Map.Entry personEntry : this.people.entrySet()) {
            PersonModel person = (PersonModel) personEntry.getValue();
            if (person.getFirstName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    person.getLastName().toLowerCase().contains(searchQuery.toLowerCase())) {
                output.add(person);
            }
        }

        return output;
    }

    public List<EventModel> searchForEvents(String searchQuery) {
        List<EventModel> output = new ArrayList<>();
        for (Map.Entry eventEntry : filteredEvents.entrySet()) {
            EventModel event = (EventModel) eventEntry.getValue();
            if (event.getCountry().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    event.getCity().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    event.getEventType().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    event.getYear().toString().contains(searchQuery)) {
                output.add(event);
            }
        }

        return output;
    }

    public String getEventInfo(EventModel event) {
        String eventType = event.getEventType().toUpperCase();
        String location = event.getCity() + ", " + event.getCountry();
        String eventYear = event.getYear().toString();

        return eventType + ": " + location + " (" + eventYear + ")";
    }

    private HashMap<String, List<PersonModel>> setImmediateFamily(PersonModel[] persons) {
        HashMap<String, List<PersonModel>> output = new HashMap<>();
        for (PersonModel person : persons) {
            output.put(person.getPersonID(), new ArrayList<>());
            if (person.getFatherID() != null) {
                output.get(person.getPersonID()).add(getPersonByID(person.getFatherID()));
            }
            if (person.getMotherID() != null) {
                output.get(person.getPersonID()).add(getPersonByID(person.getMotherID()));
            }
            if (person.getSpouseID() != null) {
                output.get(person.getPersonID()).add(getPersonByID(person.getSpouseID()));
            }
            if (childrenOfPerson.containsKey(person.getPersonID())) {
                List<String> childrenIDs = childrenOfPerson.get(person.getPersonID());
                for (String childID : childrenIDs) {
                    output.get(person.getPersonID()).add(getPersonByID(childID));
                }
            }
        }

        return output;
    }

    public List<PersonModel> getImmediateFamily(String personID) { return immediateFamily.get(personID); }


    private HashMap<String, Float> setMarkerColors(EventModel[] events) {
        HashMap<String, Float> output = new HashMap<>();
        int colorIndex = 0;
        for (EventModel event : events) {
            if (!output.containsKey(event.getEventType().toLowerCase())) {
                output.put(event.getEventType().toLowerCase(), COLOR_OPTIONS[colorIndex]);
                colorIndex++;
                if (colorIndex == COLOR_OPTIONS.length - 1) {
                    colorIndex = 0;
                }
            }
        }

        return output;
    }

    public Float getColor(String eventType) {
        return markerColors.get(eventType);
    }

    public String getRelationship(String personID, String familyMemberID) {
        PersonModel person = getPersonByID(personID);
        if (person.getFatherID() != null && person.getFatherID().equals(familyMemberID)) {
            return "Father";
        } else if (person.getMotherID() != null && person.getMotherID().equals(familyMemberID)) {
            return "Mother";
        } else if (person.getSpouseID() != null && person.getSpouseID().equals(familyMemberID)) {
            return "Spouse";
        }
        else {
            for (String childID : getChildrenOfPerson().get(personID)) {
                if (familyMemberID.equals(childID)) {
                    return "Child";
                }
            }
        }

        return null;
    }

    private HashMap<String, List<String>> setChildrenOfPerson(PersonModel[] people) {
        HashMap<String, List<String>> output = new HashMap<>();
        for (PersonModel person : people) {
            if (person.getFatherID() != null) {
                if (output.containsKey(person.getFatherID())) {
                    output.get(person.getFatherID()).add(person.getPersonID());
                } else {
                    output.put(person.getFatherID(), new ArrayList<>());
                    output.get(person.getFatherID()).add(person.getPersonID());
                }
            }

            if (person.getMotherID() != null) {
                if (output.containsKey(person.getMotherID())) {
                    output.get(person.getMotherID()).add(person.getPersonID());
                } else {
                    output.put(person.getMotherID(), new ArrayList<>());
                    output.get(person.getMotherID()).add(person.getPersonID());
                }
            }
        }

        return output;
    }

    public static HashMap<String, List<String>> getChildrenOfPerson() {
        return childrenOfPerson;
    }

    private HashMap<String, EventModel> filterEvents() {
        HashMap<String, EventModel> output = new HashMap<>();

        for (Map.Entry eventEntry : this.events.entrySet()) {
            EventModel event = (EventModel) eventEntry.getValue();
            PersonModel person = getPersonByID(event.getPersonID());
            if (!isFiltered(person)) {
                output.put(event.getEventID(), event);
            }
        }

        return output;
    }

    public static HashMap<String, EventModel> getFilteredEvents() {
        return filteredEvents;
    }

    public boolean isFiltered(PersonModel person) {
        if ( (!isShowMaleEvents() && person.getGender().equals("m")) ||
                (!isShowFemaleEvents() && person.getGender().equals("f")) ||
                (!isShowPaternalEvents()
                        && getPaternalAnscestors().contains(person.getPersonID())) ||
                (!isShowMaternalEvents()
                        && getMaternalAnscestors().contains(person.getPersonID())) ) {
            return true;
        } else {
            return false;
        }
    }

    private Set<String> setMaleAncestors(PersonModel[] people) {
        Set<String> output = new TreeSet<>();
        for (PersonModel person : people) {
            if (getPersonByID(person.getPersonID()).getGender().equals("m")) {
                output.add(person.getPersonID());
            }
        }
        return output;
    }

    private Set<String> setFemaleAncestors(PersonModel[] people) {
        Set<String> output = new TreeSet<>();
        for (PersonModel person : people) {
            if (getPersonByID(person.getPersonID()).getGender().equals("f")) {
                output.add(person.getPersonID());
            }
        }
        return output;
    }

    private HashMap<String, PersonModel> setFamilyMap(PersonModel[] persons) {
        HashMap<String, PersonModel> output = new HashMap<>();
        for (PersonModel person : persons) {
            output.put(person.getPersonID(), person);
        }
        return output;
    }

    public static PersonModel getPersonByID(String personID) {
        return people.get(personID);
    }

    private HashMap<String, EventModel> setEventsMap(EventModel[] events) {
        HashMap<String, EventModel> output = new HashMap<>();
        for (EventModel event : events) {
            output.put(event.getEventID(), event);
        }
        return output;
    }

    private HashMap<String, List<EventModel>> setPersonEvents(EventModel[] events, PersonModel[] persons) {
        HashMap<String, List<EventModel>> output = new HashMap<>();
        for (PersonModel person : persons) {
            List<EventModel> personEvents = new ArrayList<>();
            for (EventModel event : events) {
                if (event.getPersonID().equals(person.getPersonID())) {
                    personEvents.add(event);
                }
            }
            List<EventModel> organizedEvents = organizeEvents(personEvents);
            output.put(person.getPersonID(), organizedEvents);
        }
        return output;
    }

    private List<EventModel> organizeEvents(List<EventModel> personEvents) {
        List<EventModel> tempList = new ArrayList<>();
        for (EventModel event : personEvents) {
            tempList.add(event);
        }
        List<EventModel> output = new ArrayList<>();

        for (EventModel event : tempList) {
            if (event.getEventType().equalsIgnoreCase("birth")) {
                output.add(event);
                tempList.remove(event);
                break;
            }
        }

        EventModel deathEvent = null;
        for (EventModel event : tempList) {
            if (event.getEventType().equalsIgnoreCase("death")) {
                deathEvent = event;
                tempList.remove(event);
                break;
            }
        }

        while (tempList.size() > 0) {
            EventModel tempEvent = null;
            int tempYear = 3000;
            for (EventModel event : tempList) {
                if (event.getYear() < tempYear) {
                    tempYear = event.getYear();
                    tempEvent = event;
                } else if (event.getYear() == tempYear) {
                    if (tempEvent.getEventType().toLowerCase().compareTo(
                            event.getEventType().toLowerCase()) < 0) {
                        tempYear = event.getYear();
                        tempEvent = event;
                    }
                }
            }
            output.add(tempEvent);
            for (EventModel event : tempList) {
                if (event.equals(tempEvent)) {
                    tempList.remove(event);
                    break;
                }
            }
        }


        if (deathEvent != null) {
            output.add(deathEvent);
        }

        return output;
    }

    public List<EventModel> getPersonEvents(String personID) { return personEvents.get(personID); }

    private List<String> setPaternalAncestors(String nextID) {
        List<String> output = new ArrayList<>();
        setPaternalAncestorsHelper(output, nextID);
        return output;
    }

    private void setPaternalAncestorsHelper(List<String> ancestors, String nextID) {
        ancestors.add(nextID);
        if (getPersonByID(nextID).getFatherID() != null) {
            setPaternalAncestorsHelper(ancestors, getPersonByID(nextID).getFatherID());
        }
        if (getPersonByID(nextID).getMotherID() != null) {
            setPaternalAncestorsHelper(ancestors, getPersonByID(nextID).getMotherID());
        }
    }

    public List<String> getPaternalAnscestors() {
        return DataCache.paternalAncestors;
    }

    public List<String> getMaternalAnscestors() {
        return DataCache.maternalAncestors;
    }

    public void setShowPaternalEvents(boolean showPaternalEvents) {
        this.showPaternalEvents = showPaternalEvents;
        this.filteredEvents = filterEvents();
    }

    public static boolean isShowPaternalEvents() {
        return showPaternalEvents;
    }

    public static void setShowSpouseLine(boolean showSpouseLine) {
        DataCache.showSpouseLine = showSpouseLine;
    }

    public boolean isShowSpouseLine() {
        return showSpouseLine;
    }

    public static void setShowFamilyTreeLines(boolean showFamilyTreeLines) {
        DataCache.showFamilyTreeLines = showFamilyTreeLines;
    }

    public boolean isShowFamilyTreeLines() {
        return showFamilyTreeLines;
    }

    public static void setShowLifeStoryLine(boolean showLifeStoryLine) {
        DataCache.showLifeStoryLine = showLifeStoryLine;
    }

    public boolean isShowLifeStoryLine() {
        return showLifeStoryLine;
    }

    public void setShowMaternalEvents(boolean showMaternalEvents) {
        this.showMaternalEvents = showMaternalEvents;
        this.filteredEvents = filterEvents();
    }

    public static boolean isShowMaternalEvents() {
        return showMaternalEvents;
    }

    public void setShowMaleEvents(boolean showMaleEvents) {
        this.showMaleEvents = showMaleEvents;
        this.filteredEvents = filterEvents();
    }

    public static boolean isShowMaleEvents() {
        return showMaleEvents;
    }

    public void setShowFemaleEvents(boolean showFemaleEvents) {
        this.showFemaleEvents = showFemaleEvents;
        this.filteredEvents = filterEvents();
    }

    public static boolean isShowFemaleEvents() {
        return showFemaleEvents;
    }

    public static void setInstance(EventModel[] events, PersonModel[] persons,
                                   String authtoken, String rootID) {
        instance = new DataCache(events, persons, authtoken, rootID);
    }

    public static DataCache getInstance() {
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }
}
