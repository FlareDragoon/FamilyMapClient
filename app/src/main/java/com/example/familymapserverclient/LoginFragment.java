package com.example.familymapserverclient;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Model.DataCache;
import Model.PersonModel;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.GetAllFamilyEventsResult;
import Result.GetFamilyResult;
import Result.LoginResult;
import Result.RegisterResult;
import Tasks.GetDataTask;
import Tasks.LoginTask;
import Tasks.RegisterTask;

public class LoginFragment extends Fragment {

    private EditText hostEditText;
    private EditText portEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private RadioGroup genderButton;
    private Button loginButton;
    private Button registerButton;

    private LoginResult loginResult;
    private RegisterResult registerResult;
    private GetFamilyResult familyResult;
    private GetAllFamilyEventsResult eventsResult;

    private String genderString = "";

    private Listener listener;

    public interface Listener {
        void notifyDone();
    }

    public void registerListener(Listener listener) {
        this.listener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        hostEditText = view.findViewById(R.id.host);
        portEditText = view.findViewById(R.id.port);
        usernameEditText = view.findViewById(R.id.username);
        passwordEditText = view.findViewById(R.id.password);
        firstNameEditText = view.findViewById(R.id.first_name);
        lastNameEditText = view.findViewById(R.id.last_name);
        emailEditText = view.findViewById(R.id.email);
        genderButton = view.findViewById(R.id.gender);
        loginButton = view.findViewById(R.id.login);
        registerButton = view.findViewById(R.id.register);

        hostEditText.addTextChangedListener(loginTextChangedListener);
        portEditText.addTextChangedListener(loginTextChangedListener);
        usernameEditText.addTextChangedListener(loginTextChangedListener);
        passwordEditText.addTextChangedListener(loginTextChangedListener);

        hostEditText.addTextChangedListener(registerTextChangedListener);
        portEditText.addTextChangedListener(registerTextChangedListener);
        usernameEditText.addTextChangedListener(registerTextChangedListener);
        passwordEditText.addTextChangedListener(registerTextChangedListener);
        firstNameEditText.addTextChangedListener(registerTextChangedListener);
        lastNameEditText.addTextChangedListener(registerTextChangedListener);
        emailEditText.addTextChangedListener(registerTextChangedListener);
        genderButton.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.male) {
                    genderString = "m";
                    setRegisterActive();
                } else if (i == R.id.female) {
                    genderString = "f";
                    setRegisterActive();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String serverHost = hostEditText.getText().toString().trim();
                String serverPort = portEditText.getText().toString().trim();

                clearLocalData();

                LoginRequest request = new LoginRequest(username,
                        password);

                Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        Gson gson = new Gson();
                        String data = bundle.getString("LoginResultJsonDataKey");
                        loginResult = gson.fromJson(data, LoginResult.class);

                        if (loginResult.isSuccess()) {
                            performGetDataTask(serverHost, serverPort, loginResult.getAuthtoken());

                        } else {
                            showFailToast();
                        }
                    }
                };

                LoginTask task = new LoginTask(uiThreadMessageHandler, serverHost,
                        serverPort, request);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String serverHost = hostEditText.getText().toString().trim();
                String serverPort = portEditText.getText().toString().trim();
                String firstName = firstNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String gender = getGenderFromButtons();

                clearLocalData();

                RegisterRequest request = new RegisterRequest(username, password, email,
                        firstName, lastName, gender);

                Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        Gson gson = new Gson();
                        String data = bundle.getString("RegisterResultJsonDataKey");
                        registerResult = gson.fromJson(data, RegisterResult.class);

                        if (registerResult.isSuccess()) {
                            performGetDataTask(serverHost, serverPort, registerResult.getAuthtoken());

                        } else {
                            showFailToast();
                        }
                    }
                };

                RegisterTask task = new RegisterTask(uiThreadMessageHandler, serverHost,
                        serverPort, request);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });

        return view;

    }

    private void clearLocalData() {
        loginResult = null;
        registerResult = null;
        familyResult = null;
        eventsResult = null;
    }

    private void showSuccessToast(String personID) {
        PersonModel user = DataCache.getInstance().getPersonByID(personID);
        String welcomeText = "Welcome " + user.getFirstName() +
                " " + user.getLastName();
        Toast.makeText(getActivity(), welcomeText, Toast.LENGTH_SHORT).show();
    }

    private void showFailToast() {
        Toast.makeText(getActivity(), R.string.request_failed, Toast.LENGTH_SHORT).show();
    }

    private void performGetDataTask(String serverHost, String serverPort, String authtoken) {
        Handler uiThreadMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle bundle = message.getData();
                Gson gson = new Gson();
                String eventData = bundle.getString("EventResultJsonDataKey");
                String familyData = bundle.getString("FamilyResultJsonDataKey");
                eventsResult = gson.fromJson(eventData, GetAllFamilyEventsResult.class);
                familyResult = gson.fromJson(familyData, GetFamilyResult.class);
                if (loginResult != null) {
                    createLoginToast();
                } else {
                    createRegisterToast();
                }
                listener.notifyDone();

            }
        };

        GetDataTask task = new GetDataTask(uiThreadMessageHandler, serverHost,
                serverPort, authtoken);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
    }

    private void createRegisterToast() {
        if (familyResult.isSuccess() && eventsResult.isSuccess()) {
            DataCache.getInstance().setInstance(eventsResult.getData(),
                    familyResult.getData(), registerResult.getAuthtoken(),
                    registerResult.getPersonID());
            showSuccessToast(registerResult.getPersonID());
        } else {
            showFailToast();
        }
    }

    private void createLoginToast() {
        if (familyResult.isSuccess() && eventsResult.isSuccess()) {
            DataCache.getInstance().setInstance(eventsResult.getData(),
                    familyResult.getData(), loginResult.getAuthtoken(),
                    loginResult.getPersonID());
            showSuccessToast(loginResult.getPersonID());
        } else {
            showFailToast();
        }
    }

    private TextWatcher loginTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // ignore
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String hostInput = hostEditText.getText().toString().trim();
            String portInput = portEditText.getText().toString().trim();
            String usernameInput = usernameEditText.getText().toString().trim();
            String passwordInput = passwordEditText.getText().toString().trim();

            loginButton.setEnabled(!hostInput.isEmpty() && !portInput.isEmpty() &&
                    !usernameInput.isEmpty() && !passwordInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {
            //ignore
        }
    };

    private TextWatcher registerTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // ignore
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            setRegisterActive();
        }

        @Override
        public void afterTextChanged(Editable s) {
            //ignore
        }
    };

    private void setRegisterActive() {
        String hostInput = hostEditText.getText().toString().trim();
        String portInput = portEditText.getText().toString().trim();
        String usernameInput = usernameEditText.getText().toString().trim();
        String passwordInput = passwordEditText.getText().toString().trim();
        String firstNameInput = firstNameEditText.getText().toString().trim();
        String lastNameInput = lastNameEditText.getText().toString().trim();
        String emailInput = emailEditText.getText().toString().trim();
        String genderInput = getGenderFromButtons();

        registerButton.setEnabled(!hostInput.isEmpty() && !portInput.isEmpty()
                && !usernameInput.isEmpty() && !passwordInput.isEmpty()
                && !firstNameInput.isEmpty() && !lastNameInput.isEmpty()
                && !emailInput.isEmpty() && !genderString.equals("") );
    }

    public String getGenderFromButtons() {
        if (genderButton.getCheckedRadioButtonId() == R.id.male) {
            return "m";
        } else if (genderButton.getCheckedRadioButtonId() == R.id.female) {
            return "f";
        } else {
            return "";
        }
    }
}