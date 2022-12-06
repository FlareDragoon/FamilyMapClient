package com.example.familymapserverclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Model.DataCache;
import Model.EventModel;
import Model.PersonModel;

public class MapFragment extends Fragment {

    private ImageView mapEventImage;
    private TextView eventPersonName;
    private TextView eventTypeLocationDate;
    private LinearLayout mapBottomTextAndImage;
    private EventModel selectedEvent = null;

    private ArrayList<Polyline> polylines = null;

    private LatLng startEvent = null;

    private GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            addEventMarkers();
            if (startEvent != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(startEvent));
            }
            if (selectedEvent != null) {
                drawPolyLines();
            }
        }
    };

    private void addEventMarkers() {
        for (Map.Entry<String, EventModel> eventEntry : DataCache.getInstance()
                .getFilteredEvents().entrySet()) {
            LatLng coordinates = new LatLng(eventEntry.getValue().getLatitude(),
                    eventEntry.getValue().getLongitude());
            map.addMarker(new MarkerOptions().position(coordinates)
                    .icon(BitmapDescriptorFactory.defaultMarker(DataCache.getInstance().
                            getColor(eventEntry.getValue().getEventType().toLowerCase()))))
                            .setTag(eventEntry.getValue());
        }

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedEvent = (EventModel) marker.getTag();
                setEventInformation();
                drawPolyLines();

                return false;
            }
        });
    }

    private void drawPolyLines() {
        if (polylines != null) {
            for (Polyline line : polylines) {
                line.remove();
            }
        }
        polylines = new ArrayList<>();

        PersonModel selectedPerson = DataCache.getInstance()
                .getPersonByID(selectedEvent.getPersonID());

        if (DataCache.getInstance().isShowSpouseLine() &&
                selectedPerson.getSpouseID() != null &&
                DataCache.getInstance().getPersonEvents(selectedPerson.getSpouseID()) != null) {
            EventModel spouseEvent = DataCache.getInstance()
                    .getPersonEvents(selectedPerson.getSpouseID()).get(0);
            LatLng personCoordinates = new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude());
            LatLng spouseCoordinates = new LatLng(spouseEvent.getLatitude(), spouseEvent.getLongitude());

            PolylineOptions options = new PolylineOptions().add(personCoordinates)
                    .add(spouseCoordinates).color(Color.YELLOW).width(10);
            Polyline line = map.addPolyline(options);
            polylines.add(line);
        }

        if (DataCache.getInstance().isShowLifeStoryLine()) {
            List<EventModel> lifeEvents = DataCache.getInstance().getPersonEvents(selectedPerson.getPersonID());
            for (int i = 0; i < lifeEvents.size() - 1; i++) {
                LatLng firstCoordinates = new LatLng(lifeEvents.get(i).getLatitude(),
                    lifeEvents.get(i).getLongitude());
                LatLng secondCoordinates = new LatLng(lifeEvents.get(i + 1).getLatitude(),
                    lifeEvents.get(i + 1).getLongitude());

                PolylineOptions options = new PolylineOptions().add(firstCoordinates)
                        .add(secondCoordinates).color(Color.RED).width(10);
                Polyline line = map.addPolyline(options);
                polylines.add(line);
            }
        }

        if (DataCache.getInstance().isShowFamilyTreeLines()) {
            int width = 10;
            if (selectedPerson.getFatherID() != null &&
                    DataCache.getInstance().getPersonEvents(selectedPerson.getFatherID()) != null) {
                LatLng personCoordinates = new LatLng(selectedEvent.getLatitude(),
                        selectedEvent.getLongitude());

                EventModel fatherEvent = DataCache.getInstance()
                        .getPersonEvents(selectedPerson.getFatherID()).get(0);
                LatLng fatherCoordinates = new LatLng(fatherEvent.getLatitude(),
                        fatherEvent.getLongitude());

                PolylineOptions options = new PolylineOptions().add(personCoordinates)
                        .add(fatherCoordinates).color(Color.GREEN).width(width);
                Polyline line = map.addPolyline(options);
                polylines.add(line);

                drawFamilyTreeLinesHelper(selectedPerson.getFatherID(), width - 2);
            }

            if (selectedPerson.getMotherID() != null &&
                    DataCache.getInstance().getPersonEvents(selectedPerson.getMotherID()) != null) {
                LatLng personCoordinates = new LatLng(selectedEvent.getLatitude(),
                        selectedEvent.getLongitude());

                EventModel motherEvent = DataCache.getInstance()
                        .getPersonEvents(selectedPerson.getMotherID()).get(0);
                LatLng motherCoordinates = new LatLng(motherEvent.getLatitude(),
                        motherEvent.getLongitude());

                PolylineOptions options = new PolylineOptions().add(personCoordinates)
                        .add(motherCoordinates).color(Color.GREEN).width(width);
                Polyline line = map.addPolyline(options);
                polylines.add(line);

                drawFamilyTreeLinesHelper(selectedPerson.getMotherID(), width - 2);
            }
        }
    }

    private void drawFamilyTreeLinesHelper(String personId, int width) {
        PersonModel person = DataCache.getPersonByID(personId);
        EventModel personEvent = DataCache.getInstance()
                .getPersonEvents(person.getPersonID()).get(0);
        LatLng personCoordinates = new LatLng(personEvent.getLatitude(),
                personEvent.getLongitude());

        if (person.getFatherID() != null && width > 0 &&
                DataCache.getInstance().getPersonEvents(person.getFatherID()) != null) {

            EventModel fatherEvent = DataCache.getInstance()
                    .getPersonEvents(person.getFatherID()).get(0);
            LatLng fatherCoordinates = new LatLng(fatherEvent.getLatitude(),
                    fatherEvent.getLongitude());

            PolylineOptions options = new PolylineOptions().add(personCoordinates)
                    .add(fatherCoordinates).color(Color.GREEN).width(width);
            Polyline line = map.addPolyline(options);
            polylines.add(line);

            drawFamilyTreeLinesHelper(person.getFatherID(), width - 2);
        }

        if (person.getMotherID() != null && width > 0 &&
                DataCache.getInstance().getPersonEvents(person.getMotherID()) != null) {

            EventModel motherEvent = DataCache.getInstance()
                    .getPersonEvents(person.getMotherID()).get(0);
            LatLng motherCoordinates = new LatLng(motherEvent.getLatitude(),
                    motherEvent.getLongitude());

            PolylineOptions options = new PolylineOptions().add(personCoordinates)
                    .add(motherCoordinates).color(Color.GREEN).width(width);
            Polyline line = map.addPolyline(options);
            polylines.add(line);

            drawFamilyTreeLinesHelper(person.getMotherID(), width - 2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapEventImage = view.findViewById(R.id.map_event_image);
        eventPersonName = view.findViewById(R.id.event_person_name);
        eventTypeLocationDate = view.findViewById(R.id.event_type_location_date);
        mapBottomTextAndImage = view.findViewById(R.id.map_bottom_text_and_image);

        Intent intent = getActivity().getIntent();
        Float startLat = intent.getFloatExtra("STARTING_LATITUDE", 90);
        Float startLng = intent.getFloatExtra("STARTING_LONGITUDE", 90);

        startEvent = new LatLng(startLat, startLng);

        String startEventJson = intent.getStringExtra("EVENT_MODEL");
        Gson gson = new Gson();
        if (startEventJson == null || startEventJson.isEmpty()) {
            eventPersonName.setText(R.string.map_select_a_marker);
            eventTypeLocationDate.setText("");
            mapBottomTextAndImage.setClickable(false);
        } else {
            selectedEvent = gson.fromJson(startEventJson, EventModel.class);
            setEventInformation();
        }

        mapBottomTextAndImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedEvent != null) {
                    Gson gson = new Gson();
                    PersonModel personToSend = DataCache.getInstance().getPersonByID(selectedEvent.getPersonID());
                    String personString = gson.toJson(personToSend);

                    Intent intent = new Intent(getActivity(), PersonActivity.class);
                    intent.putExtra("PERSON_MODEL", personString);
                    startActivity(intent);
                }

            }
        });

        return view;
    }

    private void setEventInformation() {
        PersonModel associatedPerson = DataCache.getInstance().getPersonByID(selectedEvent.getPersonID());
        String personName = associatedPerson.getFirstName() + " " + associatedPerson.getLastName();
        String gender = associatedPerson.getGender();

        String typeLocationDate = DataCache.getInstance().getEventInfo(selectedEvent);

        Drawable genderIcon;
        if (gender.equals("m")) {
            genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male).
                    colorRes(R.color.teal_700).sizeDp(40);

        } else {
            genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female).
                    colorRes(R.color.purple_200).sizeDp(40);
        }

        mapEventImage.setImageDrawable(genderIcon);
        eventPersonName.setText(personName);
        eventTypeLocationDate.setText(typeLocationDate);
        mapBottomTextAndImage.setClickable(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity().getClass() == MainActivity.class) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.menu_layout, menu);

            MenuItem seachMenuItem = menu.findItem(R.id.search_icon);
            seachMenuItem.setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_search)
                    .colorRes(R.color.white)
                    .actionBarSize());

            MenuItem settingsMenuItem = menu.findItem(R.id.settings_icon);
            settingsMenuItem.setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_gear)
                    .colorRes(R.color.white)
                    .actionBarSize());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch(menu.getItemId()) {
            case R.id.search_icon:
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.settings_icon:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(menu);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedEvent != null) {
            PersonModel person = DataCache.getInstance().getPersonByID(selectedEvent.getPersonID());
            if (DataCache.getInstance().isFiltered(person)) {
                selectedEvent = null;
                mapEventImage.setImageDrawable(null);
                eventPersonName.setText(R.string.map_select_a_marker);
                eventTypeLocationDate.setText("");
                mapBottomTextAndImage.setClickable(false);
            }
            if (map != null) {
                map.clear();
                addEventMarkers();
                if (selectedEvent != null) {
                    drawPolyLines();
                }
            }
        }
    }
}