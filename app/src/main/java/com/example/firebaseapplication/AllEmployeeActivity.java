package com.example.firebaseapplication;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebaseapplication.Adapter.EmployeeAdapter;
import com.example.firebaseapplication.Dialogs.UpdateEmployeeDialog;
import com.example.firebaseapplication.Model.EmployeeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kcode.permissionslib.main.OnRequestPermissionsCallBack;
import com.kcode.permissionslib.main.PermissionCompat;

import java.util.ArrayList;


public class AllEmployeeActivity extends AppCompatActivity {

    ProgressBar loadingLY;
    RecyclerView rv;
    ArrayList<EmployeeModel> employeeModelsList;
    EmployeeAdapter adapter;
    EmployeeModel employeeModel;

    FirebaseFirestore fireStoreDB;

    UpdateEmployeeDialog updateEmployeeDialog;
    ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_employees);

        fireStoreDB = FirebaseFirestore.getInstance();

        rv = findViewById(R.id.recyclerView);
        loadingLY = findViewById(R.id.loadingLY);
        employeeModelsList = new ArrayList<>();

        rv.setLayoutManager(new LinearLayoutManager(this));

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            try {
                                Intent intent = result.getData();
                                Uri selectedImageUri = intent.getData();

                                if (updateEmployeeDialog != null) {
                                    updateEmployeeDialog.setPhotoUri(selectedImageUri);
                                }

                            } catch (Exception e) {
                                Log.e("FileSelectorActivity", "File select error", e);
                            }

                        }
                    }
                });

        adapter = new EmployeeAdapter(this, employeeModelsList, new DataCallBack() {
            @Override
            public void Result(Object obj, String type, Object otherData) {
                employeeModel = (EmployeeModel) obj;
                int position = (int) otherData;

                if (updateEmployeeDialog == null) {
                    updateEmployeeDialog = new UpdateEmployeeDialog(AllEmployeeActivity.this, employeeModel, new DataCallBack() {
                        @Override
                        public void Result(Object obj, String type, Object otherData) {

                            if (type.equals(Constants.PICK_IMAGE)) {
                                checkPermission();
                            } else {
                                employeeModel = (EmployeeModel) obj;
                                adapter.dataList.set(position, employeeModel);
                                adapter.notifyItemChanged(position);
                            }
                        }
                    });
                    updateEmployeeDialog.setOnDismissListener(dialog -> updateEmployeeDialog = null);
                }
            }

        });

        rv.setAdapter(adapter);

        getStudentData();

    }

    public void getStudentData() {

        loadingLY.setVisibility(View.VISIBLE);

        fireStoreDB.collection(Constants.USER)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                loadingLY.setVisibility(View.GONE);

                if (task.isSuccessful()) {

                    employeeModelsList.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        EmployeeModel employeeModel = document.toObject(EmployeeModel.class);
                        employeeModelsList.add(employeeModel);
                    }
                    adapter.dataList = employeeModelsList;
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkPermission() {

        try {
            PermissionCompat.Builder builder = new PermissionCompat.Builder(this);
            builder.addPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
            builder.addPermissionRationale(getString(R.string.should_allow_permission));
            builder.addRequestPermissionsCallBack(new OnRequestPermissionsCallBack() {
                @Override
                public void onGrant() {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    pickImageLauncher.launch(Intent.createChooser(intent, ""));
                }
                @Override
                public void onDenied(String permission) {
                    Toast.makeText(AllEmployeeActivity.this, getString(R.string.some_permission_denied), Toast.LENGTH_SHORT).show();
                }
            });
            builder.build().request();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
