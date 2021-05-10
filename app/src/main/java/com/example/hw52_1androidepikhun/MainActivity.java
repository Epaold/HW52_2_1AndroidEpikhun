package com.example.hw52_1androidepikhun;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String FILE_NAME = "user.txt";

   // public static final int REQUEST_CODE_PERMISSION_WRITE_STORAGE = 11;

    private EditText editPersonName;
    private EditText editPassword;

    private HashMap<String, User> users = new HashMap<>();
    private HashSet<String> loginedUsers = new HashSet<>();

    private SharedPreferences switchSharedPref;
    private int switchState;
    private final int INTERNAL_MEMORY = 0;
    private final int EXTERNAL_MEMORY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        int permissionStatus = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permissionStatus == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_CODE_PERMISSION_WRITE_STORAGE);
//        }


        InitViews();
    }


    private void loadUsersFromInternal() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(openFileInput(FILE_NAME)));
            try {
                String readString;
                while ((readString = reader.readLine()) != null) {
                    String[] parts = readString.split(";");
                    users.put(parts[0], new User(parts[0], parts[1]));
                }
                Toast.makeText(MainActivity.this, getString(R.string.users_db_loaded_internal, users.size()), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private void loadUsersFromExternal() {
        BufferedReader bufferedReader;
        FileReader fileReader;
        //   File fileToLoad = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);

        File fileToLoad = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);

        try {
            fileReader = new FileReader(fileToLoad);
            bufferedReader = new BufferedReader(fileReader);
            try {
                String readString;
                while ((readString = bufferedReader.readLine()) != null) {
                    String[] parts = readString.split(";");
                    users.put(parts[0], new User(parts[0], parts[1]));
                }
                Toast.makeText(MainActivity.this, getString(R.string.users_db_loaded_external, users.size()), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void loginUser(String userName, String password) {
        if (userName.length() == 0) {
            Toast.makeText(MainActivity.this, getString(R.string.enter_username), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() == 0) {
            Toast.makeText(MainActivity.this, getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!users.containsKey(userName)) {
            Toast.makeText(MainActivity.this, getString(R.string.user_is_not_exist), Toast.LENGTH_SHORT).show();
            return;
        }
        if (loginedUsers.contains(userName)) {
            Toast.makeText(MainActivity.this, getString(R.string.user_is_logined_already), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(Objects.requireNonNull(users.get(userName)).getPassword())) {
            Toast.makeText(MainActivity.this, getString(R.string.password_is_wrong), Toast.LENGTH_SHORT).show();
            return;
        }
        loginedUsers.add(userName);
        Toast.makeText(MainActivity.this, getString(R.string.user_is_logined), Toast.LENGTH_SHORT).show();
    }


    private void loadUsers() {
        if (switchState == INTERNAL_MEMORY) loadUsersFromInternal();
        else loadUsersFromExternal();
    }

    private void saveUsers(boolean toastShow) {
        if (switchState == INTERNAL_MEMORY) saveUsersToInternal(toastShow);
        else saveUsersToExternal(toastShow);
    }


    private void saveUsersToInternal(boolean toastShow) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(FILE_NAME, MODE_PRIVATE)));) {
            for (HashMap.Entry<String, User> user : users.entrySet()) {
                writer.write(String.format("%s;%s\n", user.getValue().getUserName(), user.getValue().getPassword()));
            }
            if (toastShow)
                Toast.makeText(MainActivity.this, getString(R.string.new_user_is_registered), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void saveUsersToExternal(boolean toastShow) {
        if (isExternalStorageWritable()) {

            //  File fileToLoad = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
            //   File fileToSave = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);

            File fileToSave = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);

            FileWriter fileWriter;
            try {
                fileWriter = new FileWriter(fileToSave, false);
                for (HashMap.Entry<String, User> user : users.entrySet()) {
                    fileWriter.write(String.format("%s;%s\n", user.getValue().getUserName(), user.getValue().getPassword()));
                }
                fileWriter.close();
                if (toastShow)
                    Toast.makeText(MainActivity.this, getString(R.string.new_user_is_registered), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private void deleteExternalFile() {
        if (isExternalStorageWritable()) {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
            file.delete();
        }
    }

    private void deleteInternalFile() {
        File file = new File(getFilesDir(), FILE_NAME);
        file.delete();
    }


    private void registerUser(String userName, String password) {
        if (userName.length() == 0) {
            Toast.makeText(MainActivity.this, getString(R.string.enter_register_usernamer), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() == 0) {
            Toast.makeText(MainActivity.this, getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (users.containsKey(userName)) {
            Toast.makeText(MainActivity.this, getString(R.string.user_is_exist), Toast.LENGTH_SHORT).show();
            return;
        }
        users.put(userName, new User(userName, password));
        saveUsers(true);

    }

    private void InitViews() {
        Button loginButton = findViewById(R.id.buttonLogin);

        Button registerButton = findViewById(R.id.buttonRegister);
        editPersonName = findViewById(R.id.editPersonName);
        editPassword = findViewById(R.id.editPassword);


        Switch memorySwitch = findViewById(R.id.memorySwitch);

        switchSharedPref = getSharedPreferences("memoryswitch", MODE_PRIVATE);


        switchState = INTERNAL_MEMORY;
        if (switchSharedPref.contains("memory")) {
            String state = switchSharedPref.getString("memory", "");
            if ("external".equals(state)) {
                switchState = EXTERNAL_MEMORY;
                memorySwitch.setChecked(true);
            } else {
                memorySwitch.setChecked(false);
            }
        }

        loadUsers();

        memorySwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            SharedPreferences.Editor editor = switchSharedPref.edit();
            if (isChecked) {
                switchState = EXTERNAL_MEMORY;
                editor.putString("memory", "external");
                editor.apply();
                Toast.makeText(MainActivity.this, "Выбрана внешняя память", Toast.LENGTH_SHORT).show();
                saveUsers(false);
                deleteInternalFile();
                loginedUsers.remove(editPersonName.getText().toString());
            } else {
                switchState = INTERNAL_MEMORY;
                editor.putString("memory", "internal");
                editor.apply();
                Toast.makeText(MainActivity.this, "Выбрана внутренняя память", Toast.LENGTH_SHORT).show();
                saveUsers(false);
                deleteExternalFile();
                loginedUsers.remove(editPersonName.getText().toString());
            }

        });

        Button.OnClickListener onClickListener = view -> {
            String userName = editPersonName.getText().toString();
            String password = editPassword.getText().toString();
            if (view.getId() == R.id.buttonLogin) loginUser(userName, password);
            if (view.getId() == R.id.buttonRegister) registerUser(userName, password);
        };
        loginButton.setOnClickListener(onClickListener);
        registerButton.setOnClickListener(onClickListener);

    }
}