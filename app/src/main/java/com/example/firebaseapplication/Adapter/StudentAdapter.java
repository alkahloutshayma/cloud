package com.example.firebaseapplication.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firebaseapplication.Constants;
import com.example.firebaseapplication.DataCallBack;
import com.example.firebaseapplication.Model.StudentModel;
import com.example.firebaseapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    Activity context;
    public ArrayList<StudentModel> dataList;
    DataCallBack dataCallBack;
    FirebaseFirestore fireStoreDB = FirebaseFirestore.getInstance();

    public StudentAdapter(Activity context, ArrayList<StudentModel> data, DataCallBack callBack) {
        this.context = context;
        this.dataList = data;
        this.dataCallBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.employee_item, parent, false);

        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        StudentModel studentModel = dataList.get(position);

        holder.tvName.setText(String.valueOf(studentModel.name));
        holder.avarg.setText(String.valueOf(studentModel.average));
        holder.jobTime.setText(String.valueOf(studentModel.jobTime));
        holder.holiday.setText(String.valueOf(studentModel.holiday));

        Glide.with(context).asBitmap().load(studentModel.photo).placeholder(R.drawable.profile).into(holder.img);

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView idTv, tvName, avarg, holiday;
        ImageView img;
        TextView jobTime;
        Button okBtn, cancelBtn;
        CardView cardView;
        FloatingActionButton floatbtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            idTv = itemView.findViewById(R.id.idTv);
            holiday = itemView.findViewById(R.id.holiday);
            tvName = itemView.findViewById(R.id.tvTitle);
            avarg = itemView.findViewById(R.id.userAvarg);
            img = itemView.findViewById(R.id.profileImg);
            jobTime = itemView.findViewById(R.id.jobTime);
            cardView = itemView.findViewById(R.id.cardItem);
            okBtn = itemView.findViewById(R.id.okBtn);
            cancelBtn = itemView.findViewById(R.id.cancelBtn);
            floatbtn = itemView.findViewById(R.id.dotBtn);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StudentModel studentModel = dataList.get(getAdapterPosition());
                    dataCallBack.Result(studentModel, "", getAdapterPosition());
                }
            });

            floatbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StudentModel studentModel = dataList.get(getAdapterPosition());
                    int holiday = Integer.parseInt(studentModel.holiday)*10;
                    double finalSalary = (studentModel.average)-holiday ;

                    Toast.makeText(context, "your salary is : "+ finalSalary, Toast.LENGTH_SHORT).show();

                }
            });

            cardView.setOnLongClickListener(v -> {

                StudentModel studentModel = dataList.get(getAdapterPosition());

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                StudentDB dataAccess = new StudentDB(context);
                builder.setTitle("Delete Student");
                builder.setMessage("Are you sure to delete delete");

                builder.setPositiveButton("Yes", (dialog, which) -> {
                    int position = getAdapterPosition();
//                    dataAccess.deleteStudent(studentModel.getId());

                    fireStoreDB.collection(Constants.USER).document(studentModel.id)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG", "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("TAG", "Error deleting document", e);
                                }
                            });

                    dataList.remove(position);
                    notifyItemRemoved(position);
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                });
                builder.create().show();
                return false;
            });
        }
    }

}
