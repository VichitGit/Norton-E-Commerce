package vichitpov.com.fbs.ui.activity.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import dmax.dialog.SpotsDialog;
import fr.ganfra.materialspinner.MaterialSpinner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vichitpov.com.fbs.R;
import vichitpov.com.fbs.preference.UserInformationManager;
import vichitpov.com.fbs.retrofit.response.UserInformationResponse;
import vichitpov.com.fbs.retrofit.service.ApiService;
import vichitpov.com.fbs.retrofit.service.ServiceGenerator;
import vichitpov.com.fbs.ui.activity.login.StartLoginActivity;

public class EditUserProfileActivity extends AppCompatActivity {
    private EditText editFirstName, editLastName, editAddress, editCity;
    private MaterialSpinner spinnerGender;
    private UserInformationManager userInformationManager;
    private String selectedGender = "null";
    private Button buttonSave;
    private SpotsDialog dialog;
    //private int PLACE_PICKER_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        spinnerGender = findViewById(R.id.spinner);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editAddress = findViewById(R.id.editAddress);
        editCity = findViewById(R.id.editCity);
        buttonSave = findViewById(R.id.buttonSave);

        userInformationManager = UserInformationManager.getInstance(getSharedPreferences(UserInformationManager.PREFERENCES_USER_INFORMATION, MODE_PRIVATE));
        dialog = new SpotsDialog(this, "Updating...");

        getSharePreferences();
        setUpGender();

        listener();


    }

    private void listener() {
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0)
                    selectedGender = "Male";
                else
                    selectedGender = "Female";
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        buttonSave.setOnClickListener(view -> {

            if (checkValidation()) {

                ApiService apiService = ServiceGenerator.createService(ApiService.class);
                String accessToken = userInformationManager.getUser().getAccessToken();

                if (!accessToken.equals("N/A")) {
                    dialog.show();
                    Call<UserInformationResponse> call = apiService.updateUser(accessToken, editFirstName.getText().toString(),
                            editLastName.getText().toString(), selectedGender, editAddress.getText().toString(),
                            editCity.getText().toString(), userInformationManager.getUser().getProfile());

                    call.enqueue(new Callback<UserInformationResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UserInformationResponse> call, @NonNull Response<UserInformationResponse> response) {
                            if (response.isSuccessful()) {
                                userInformationManager.deleteUserInformation();
                                userInformationManager.saveInformation(response.body());
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));

                            } else if (response.code() == 401) {
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), StartLoginActivity.class));

                            } else {
                                dialog.dismiss();
                                Log.e("pppp else", response.code() + " = " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UserInformationResponse> call, @NonNull Throwable t) {
                            dialog.dismiss();
                            t.printStackTrace();
                            Log.e("pppp onFailure", t.getMessage());

                        }
                    });

                }
            }

        });
    }


    private boolean checkValidation() {
        if (editFirstName.getText().toString().isEmpty() && editLastName.getText().toString().isEmpty() && editAddress.getText().toString().isEmpty() && editCity.getText().toString().isEmpty()) {
            editFirstName.setError("Required first name");
            editFirstName.requestFocus();
            editLastName.setError("Required last name");
            editAddress.setError("Required address");
            editCity.setError("Required city");
            return false;
        } else {
            if (editFirstName.getText().toString().isEmpty()) {
                editFirstName.requestFocus();
                editFirstName.setError("Required first name");
            } else if (editLastName.getText().toString().isEmpty()) {
                editLastName.requestFocus();
                editLastName.setError("Required last name");
            } else if (editAddress.getText().toString().isEmpty()) {
                editAddress.requestFocus();
                editAddress.setError("Required address");
            } else if (editCity.getText().toString().isEmpty()) {
                editCity.requestFocus();
                editCity.setError("Required city");
            } else {
                return true;
            }
            return false;
        }
    }


    private void setUpGender() {
        String[] ITEMS = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void getSharePreferences() {
        String pFirstName = userInformationManager.getUser().getFirstName();
        String pLastName = userInformationManager.getUser().getLastName();
        String pAddress = userInformationManager.getUser().getAddress();
        String pGender = userInformationManager.getUser().getGender();
        String pCity = userInformationManager.getUser().getCity();

        if (!pFirstName.equals("N/A") && !pLastName.equals("N/A")) {
            editFirstName.setText(pFirstName);
            editLastName.setText(pLastName);
        }

        if (!pAddress.equals("N/A")) {
            editAddress.setText(pAddress);
        }

        if (!pGender.equals("N/A")) {
            if (pGender.equals("Male") || pGender.equals("male"))
                spinnerGender.setSelection(0);
            else if (pGender.equals("Female") || pGender.equals("female"))
                spinnerGender.setSelection(1);
        }

        if (!pCity.equals("N/A")) {
            editCity.setText(pCity);
        }


    }


//    @Override
//    public void onClick(View view) {
////        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
////        try {
////            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
////        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
////            e.printStackTrace();
////        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Place place = PlacePicker.getPlace(getApplicationContext(), data);
//                String address = place.getAddress().toString();
//
//                Log.e("pppp Address: ", address);
//                Log.e("pppp ID:", place.getId());
//                Log.e("pppp Latitude:", place.getLatLng().latitude + "");
//                Log.e("pppp Longitude:", place.getLatLng().longitude + "");
//                Log.e("pppp PhoneNumber:", place.getPhoneNumber().toString());
//                Log.e("pppp PlaceTypes:", place.getPlaceTypes().toString());
//
//                textAddress.setText(address);
//            }
//        }
    }
}
