package com.example.bloodglucosetracker.ui.bloodSugarToday;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.bloodglucosetracker.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BloodSugarTodayFragment extends Fragment {
    private static final String SERVER_IP = "INSERT IP HERE";
    private static final int SERVER_PORT = 8000;
    private static final int[] NOTIFICATION_TIMES = {7, 13, 19, 23};
    private TextView textViewDate;
    private TextView textViewTime;
    private EditText editTextBloodSugarValue;
    private Button buttonSaveBloodSugar;
    private String loginInfo;
    private String formattedDate;
    private int clickCount = 0;
    private int[] bloodSugarValues = new int[4];

    private String action="INSERT-";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bloodsugartoday, container, false);

        textViewDate = root.findViewById(R.id.textViewDate);
        textViewTime = root.findViewById(R.id.textViewTime);
        editTextBloodSugarValue = root.findViewById(R.id.editTextBloodSugarValue);
        buttonSaveBloodSugar = root.findViewById(R.id.buttonSaveBloodSugar);

        loginInfo = getActivity().getIntent().getStringExtra("loginInfo");

        showBloodSugarSchedule();

        buttonSaveBloodSugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increment click count
                clickCount++;

                // Get the entered blood sugar value
                int bloodSugarValue = Integer.parseInt(editTextBloodSugarValue.getText().toString());
                // Store the blood sugar value
                bloodSugarValues[clickCount - 1] = bloodSugarValue;

                if(clickCount==0){
                    textViewTime.setText("Blood sugar value for: 7 am");
                    editTextBloodSugarValue.setText("");
                }
                else if(clickCount==1){
                    textViewTime.setText("Blood sugar value for: 1 pm");
                    editTextBloodSugarValue.setText("");

                }
                else if(clickCount==2){
                    textViewTime.setText("Blood sugar value for: 7 pm");
                    editTextBloodSugarValue.setText("");

                }
                else if(clickCount==3){
                    textViewTime.setText("Blood sugar value for: before bedtime");
                    editTextBloodSugarValue.setText("");

                }
                // Check if all values are collected
                if (clickCount == 4) {
                    // Send blood sugar values, formatted date, and login information to the server
                    sendMessageToServer(action,loginInfo, formattedDate, bloodSugarValues);
                    // Reset click count
                    clickCount = 0;
                    textViewTime.setText("That's it, all done for today!");
                    editTextBloodSugarValue.setVisibility(View.GONE);

                    scheduleNotifications();

                }

            }
        });

        return root;

    }

    private void showBloodSugarSchedule() {
        // Get today's date
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        formattedDate = dateFormat.format(today); // Store the formatted date

        textViewDate.setText("Today's Date: " + formattedDate);

        textViewTime.setText("Blood sugar value for: 7 am");
    }

    private void scheduleNotifications() {
        Log.d("BloodSugarTodayFragment", "Scheduling notifications...");

        for (int notificationTime : NOTIFICATION_TIMES) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, notificationTime);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
            WorkRequest notificationRequest = new OneTimeWorkRequest.Builder(NotificationForegroundService.NotificationWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(requireContext()).enqueue(notificationRequest);
        }
    }

    private void sendMessageToServer(String action,final String loginInfo, final String formattedDate, final int[] bloodSugarValues) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Socket socket = new Socket(SERVER_IP, SERVER_PORT);

                    StringBuilder messageToBeSent = new StringBuilder(action);
                    messageToBeSent.append(loginInfo);
                    messageToBeSent.append("-").append(formattedDate);
                    for (int value : bloodSugarValues) {
                        messageToBeSent.append("-").append(value);
                    }

                    // Send data to the server
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(messageToBeSent);

                    // Receive data from the server
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String receivedMessage = in.readLine();

                    // Close the connection
                    socket.close();

                    return receivedMessage;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Toast.makeText(getContext(), "Message sent to server successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to send message to server", Toast.LENGTH_SHORT).show();
                }

                // Check if the day is still the same as when the bedtime data was inserted
                Date today = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                String currentDate = dateFormat.format(today);
                if (currentDate.equals(formattedDate)) {
                    // Navigate back to the menu
                    getActivity().onBackPressed();
                }
            }
        }.execute();
    }



}


