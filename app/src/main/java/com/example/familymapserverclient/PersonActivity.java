package com.example.familymapserverclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.List;

import Model.DataCache;
import Model.EventModel;
import Model.PersonModel;

public class PersonActivity extends AppCompatActivity {

    private TextView personFirstName;
    private TextView personLastName;
    private TextView personGender;

    private ExpandableListView expandableListView;

    private PersonModel person;

    public PersonModel getPerson() { return person; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Iconify.with(new FontAwesomeModule());

        Gson gson = new Gson();
        Intent intent = getIntent();

        String personString = intent.getStringExtra("PERSON_MODEL");
        person = gson.fromJson(personString, PersonModel.class);

        personFirstName = findViewById(R.id.person_first_name);
        personLastName = findViewById(R.id.person_last_name);
        personGender = findViewById(R.id.person_gender);

        expandableListView = findViewById(R.id.expandable_list_view);

        personFirstName.setText(person.getFirstName());
        personLastName.setText(person.getLastName());
        if (person.getGender().equals("m")) {
            personGender.setText(R.string.male);
        } else {
            personGender.setText(R.string.female);
        }
        List<PersonModel> immediateFamily = DataCache.getInstance().getImmediateFamily(person.getPersonID());
        List<EventModel> lifeEvents;
        if (DataCache.getInstance().isFiltered(person)) {
            lifeEvents = new ArrayList<>();
        } else {
            lifeEvents = DataCache.getInstance().getPersonEvents(person.getPersonID());
        }

        expandableListView.setAdapter(new ExpandableListAdapter(immediateFamily, lifeEvents));
    }


    class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int FAMILY_GROUP_POSITION = 0;
        private static final int EVENT_GROUP_POSITION = 1;

        private final List<PersonModel> family;
        private final List<EventModel> events;

        ExpandableListAdapter(List<PersonModel> family, List<EventModel> events) {
            this.family = family;
            this.events = events;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case FAMILY_GROUP_POSITION:
                    return family.size();
                case EVENT_GROUP_POSITION:
                    return events.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
                case FAMILY_GROUP_POSITION:
                return getString(R.string.family_title);
                case EVENT_GROUP_POSITION:
                return getString(R.string.life_events_title);
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case FAMILY_GROUP_POSITION:
                    return family.get(childPosition);
                case EVENT_GROUP_POSITION:
                    return events.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.expandable_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case FAMILY_GROUP_POSITION:
                titleView.setText(R.string.family_title);
                    break;
                case EVENT_GROUP_POSITION:
                titleView.setText(R.string.life_events_title);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch(groupPosition) {
                case FAMILY_GROUP_POSITION:
                itemView = getLayoutInflater().inflate(R.layout.person_list_item, parent, false);
                initializeFamilyView(itemView, childPosition);
                    break;
                case EVENT_GROUP_POSITION:
                itemView = getLayoutInflater().inflate(R.layout.event_list_item, parent, false);
                initializeEventsView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
        }

        private void initializeFamilyView(View personItemView, final int childPosition) {
            ImageView personListImage = personItemView.findViewById(R.id.person_list_image);

            Drawable genderIcon;
            if (family.get(childPosition).getGender().equals("m")) {
                genderIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_male).
                        colorRes(R.color.teal_700).sizeDp(40);
            } else {
                genderIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_female).
                        colorRes(R.color.purple_200).sizeDp(40);
            }
            personListImage.setImageDrawable(genderIcon);

            TextView personListName = personItemView.findViewById(R.id.person_list_name);
            String fullName = family.get(childPosition).getFirstName() + " " +
                    family.get(childPosition).getLastName();
            personListName.setText(fullName);

            TextView personListTitle = personItemView.findViewById(R.id.person_list_title);
            String relationship = DataCache.getInstance().getRelationship(person.getPersonID(),
                    family.get(childPosition).getPersonID());
            personListTitle.setText(relationship);

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    Gson gson = new Gson();

                    String personJson = gson.toJson(family.get(childPosition));
                    intent.putExtra("PERSON_MODEL", personJson);
                    startActivity(intent);
                }
            });
        }

        private void initializeEventsView(View eventItemView, final int childPosition) {
            ImageView eventListImage = eventItemView.findViewById(R.id.event_list_image);
            Drawable markerIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_map_marker).
                    colorRes(R.color.black).sizeDp(40);
            eventListImage.setImageDrawable(markerIcon);

            TextView eventListInfo = eventItemView.findViewById(R.id.event_list_info);
            EventModel eventToDisplay = events.get(childPosition);
            String eventInfo = DataCache.getInstance().getEventInfo(eventToDisplay);
            eventListInfo.setText(eventInfo);

            TextView eventListPersonName = eventItemView.findViewById(R.id.event_list_person_name);
            PersonModel personnToDisplay = DataCache.getInstance().getPersonByID(events
                    .get(childPosition).getPersonID());
            String fullName = personnToDisplay.getFirstName() + " " + personnToDisplay.getLastName();
            eventListPersonName.setText(fullName);

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    Gson gson = new Gson();

                    String eventJson = gson.toJson(events.get(childPosition));
                    intent.putExtra("STARTING_LATITUDE", events.get(childPosition).getLatitude());
                    intent.putExtra("STARTING_LONGITUDE", events.get(childPosition).getLongitude());
                    intent.putExtra("EVENT_MODEL", eventJson);
                    startActivity(intent);
                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }
}