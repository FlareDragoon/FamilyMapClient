package com.example.familymapserverclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.List;

import Model.DataCache;
import Model.EventModel;
import Model.PersonModel;

public class SearchActivity extends AppCompatActivity {

    private static final int PERSON_ITEM_VIEW_TYPE = 0;
    private static final int EVENT_ITEM_VIEW_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));



        SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange (String newText) {
                renewRecycleView(newText);
                return false;
            }

            private void renewRecycleView(String searchQuery) {
                List<PersonModel> people = DataCache.getInstance().searchForPeople(searchQuery);
                List<EventModel> events = DataCache.getInstance().searchForEvents(searchQuery);

                SearchResultsAdapter adapter = new SearchResultsAdapter(people, events);
                recyclerView.setAdapter(adapter);
            }
        };

        SearchView searchView = findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(searchListener);


    }



    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsViewHolder> {
        private final List<PersonModel> people;
        private final List<EventModel> events;

        SearchResultsAdapter(List<PersonModel> people, List<EventModel> events) {
            this.people = people;
            this.events = events;
        }

        @Override
        public int getItemViewType(int position) {
            return position < people.size() ? PERSON_ITEM_VIEW_TYPE : EVENT_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchResultsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.person_list_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.event_list_item, parent, false);
            }

            return new SearchResultsViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultsViewHolder holder, int position) {
            if(position < people.size()) {
                holder.bind(people.get(position));
            } else {
                holder.bind(events.get(position - people.size()));
            }
        }

        @Override
        public int getItemCount() {
            return people.size() + events.size();
        }
    }

    private class SearchResultsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView itemIcon;
        private final TextView name;
        private final TextView eventInfo;

        private final int viewType;
        private PersonModel person;
        private EventModel event;

        SearchResultsViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                itemIcon = itemView.findViewById(R.id.person_list_image);
                name = itemView.findViewById(R.id.person_list_name);
                eventInfo = null;
            } else {
                itemIcon = itemView.findViewById(R.id.event_list_image);
                name = itemView.findViewById(R.id.event_list_person_name);
                eventInfo = itemView.findViewById(R.id.event_list_info);
            }
        }

        private void bind(PersonModel person) {
            this.person = person;
            Drawable genderIcon;
            if (person.getGender().equals("m")) {
                genderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_male).
                        colorRes(R.color.teal_700).sizeDp(40);
            } else {
                genderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_female).
                        colorRes(R.color.purple_200).sizeDp(40);
            }
            itemIcon.setImageDrawable(genderIcon);
            name.setText(person.getFirstName() + " " + person.getLastName());
        }

        private void bind(EventModel event) {
            this.event = event;
            itemIcon.setImageDrawable(new IconDrawable(SearchActivity.this,
                    FontAwesomeIcons.fa_map_marker).colorRes(R.color.black).sizeDp(40));
            PersonModel person = DataCache.getInstance().getPersonByID(event.getPersonID());
            name.setText(person.getFirstName() + " " + person.getLastName());
            eventInfo.setText(DataCache.getInstance().getEventInfo(event));
        }

        @Override
        public void onClick(View view) {
            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                Gson gson = new Gson();
                String personJson = gson.toJson(person);

                Intent intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra("PERSON_MODEL", personJson);
                startActivity(intent);

            } else {
                Gson gson = new Gson();
                String eventJson = gson.toJson(event);

                Intent intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra("STARTING_LATITUDE", event.getLatitude());
                intent.putExtra("STARTING_LONGItUDE", event.getLongitude());
                intent.putExtra("EVENT_MODEL", eventJson);

                startActivity(intent);
            }
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