package com.example.firebaseapplication.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.firebaseapplication.Constants;
import com.example.firebaseapplication.DataCallBack;
import com.example.firebaseapplication.Model.EmployeeModel;
import com.example.firebaseapplication.R;
import com.example.firebaseapplication.databinding.DialogUpdateStudentBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateEmployeeDialog extends Dialog {

    Activity activity;
    int holidayCount;
    Uri updatProfileImgeUri;
    DialogUpdateStudentBinding binding;
    FirebaseFirestore fireStoreDB;
    StorageReference storageRef;

    EmployeeModel employeeModel;
    DataCallBack okCall;

    public UpdateEmployeeDialog(Activity context, EmployeeModel student, final DataCallBack okCall) {
        super(context);
        activity = context;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        binding = DialogUpdateStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        employeeModel = student;
        this.okCall = okCall;
        fireStoreDB = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        binding.loadingLY.setVisibility(View.GONE);

//        progressDialog = new ProgressDialog(activity);
//        progressDialog.setMessage(activity.getString(R.string.edit_student));

        binding.tvUserName.setText(employeeModel.name);
        binding.edAvarg.setText(String.valueOf(employeeModel.salary));
        holidayCount = Integer.parseInt(employeeModel.holiday);
        binding.holiday.setText(employeeModel.holiday);

        if (employeeModel.photo != null) {
            Glide.with(activity)
                    .asBitmap()
                    .load(employeeModel.photo)
                    .into(binding.updatProfileImg);
        }
//            setPhotoUri(Uri.parse(studentModel.photo));

        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holidayCount++;
                employeeModel.holiday = String.valueOf(holidayCount);
                binding.holiday.setText(employeeModel.holiday);
            }
        });

        binding.okBtn.setOnClickListener(view -> {

            String userNameStr = binding.tvUserName.getText().toString().trim();
            String avrgStr = binding.edAvarg.getText().toString().trim();

            // here check all fields that is not null on empty

            boolean hasError = false;
            if (userNameStr.isEmpty()) {
                binding.tvUserName.setError(activity.getString(R.string.invalid_input));
                hasError = true;
            }
            if (avrgStr.isEmpty()) {
                binding.edAvarg.setError(activity.getString(R.string.invalid_input));
                hasError = true;
            }
            if (hasError)
                return;

            employeeModel.name = userNameStr;
            employeeModel.salary = Double.parseDouble(avrgStr);
            employeeModel.holiday = String.valueOf(holidayCount);

            if (updatProfileImgeUri != null) {
                uploadPhoto(updatProfileImgeUri);
            } else {
                updateStudentData();
            }
        });

        binding.cancelBtn.setOnClickListener(view -> {

            dismiss();
        });

        binding.updatProfileImg.setOnClickListener(view -> {

            if (okCall != null) {
                okCall.Result(null, Constants.PICK_IMAGE, null);
            }
        });

        try {
            if (activity != null && !activity.isFinishing())
                show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void setPhotoUri(Uri photoUri) {

        updatProfileImgeUri = photoUri;

        Glide.with(activity)
                .asBitmap()
                .load(updatProfileImgeUri)
                .into(binding.updatProfileImg);
    }


    private void uploadPhoto(Uri photoUri) {

        StorageReference imgRef = storageRef.child(Constants.IMAGES + "/"
                + UUID.randomUUID().toString());
        binding.loadingLY.setVisibility(View.VISIBLE);


        UploadTask uploadTask = imgRef.putFile(photoUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("aa", exception + "");
//                GlobalHelper.hideProgressDialog();
                // Handle unsuccessful uploads
                Toast.makeText(activity, "ddddd", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(taskSnapshot -> {

            imgRef.getDownloadUrl().addOnCompleteListener(task -> {

                employeeModel.photo = task.getResult().toString();
                Log.i("s", "Log photo " + employeeModel.photo);
                updateStudentData();
//                System.out.println("Log uploaded url " + studentModel.getphoto());
                binding.loadingLY.setVisibility(View.GONE);
            });


        });
    }

    private void updateStudentData() {

        Map<String, Object> studentMap = new HashMap<>();
        studentMap.put("id", employeeModel.id);
        studentMap.put("name", employeeModel.name);
        studentMap.put("photo", employeeModel.photo);
        studentMap.put("average", employeeModel.salary);
        studentMap.put("holiday", employeeModel.holiday);

//        progressDialog.show();
        fireStoreDB.collection(Constants.USER).document(employeeModel.id)
                .update(studentMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    //                    progressDialog.d
                    @Override

                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Log DocumentSnapshot successfully deleted!");
                        dismiss();

                        if (okCall != null) {
                            okCall.Result(employeeModel, "", null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "Log Error deleting document", e);

                        Toast.makeText(activity, "Fail edit student", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}