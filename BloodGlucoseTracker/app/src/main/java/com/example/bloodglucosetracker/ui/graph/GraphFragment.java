package com.example.bloodglucosetracker.ui.graph;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bloodglucosetracker.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GraphFragment extends Fragment {
    private static final String SERVER_IP = "INSERT IP";
    private static final int SERVER_PORT = 8000;
    private String loginInfo;
    private String action = "RETRIEVE-";
    Button sendmail;
    private List<DataEntry> data;

    private ArrayList<String> ArrayToDoc = new ArrayList<>();
    public static GraphFragment newInstance(String loginInfo) {
        Log.d("GraphFragment", "newInstance called with loginInfo: " + loginInfo);
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putString("loginInfo", loginInfo);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_graph, container, false);
        sendmail=root.findViewById(R.id.sendmail);
        if (getArguments() != null) {
            loginInfo = getArguments().getString("loginInfo");
            Log.i("oncreateview:", "if argument not null");

            fetchDataFromServer();
            sendmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendEmail();
                }
            });
        }

        return root;
    }


    private void fetchDataFromServer() {
        new FetchDataFromServerTask().execute();
    }

    private class FetchDataFromServerTask extends AsyncTask<Void, Void, List<DataEntry>> {

        @Override
        protected List<DataEntry> doInBackground(Void... voids) {
            List<DataEntry> data = new ArrayList<>();

            try {
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(action + loginInfo + "-" + "data" + "-" + "4" + "-" + "3" + "-" + "2" + "-" + "1");
                Log.i("fetchdata:", action + loginInfo);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response;
                while ((response = in.readLine()) != null) {
                    String[] parts = response.split("-");
                    String date = parts[0];
                    int value1 = Integer.parseInt(parts[1]);
                    int value2 = Integer.parseInt(parts[2]);
                    int value3 = Integer.parseInt(parts[3]);
                    int value4 = Integer.parseInt(parts[4]);

                    // Create a DataEntry object for each date
                    DataEntry entry = new DataEntry(date, value1, value2, value3, value4);
                    data.add(entry);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(List<DataEntry> fetchedData) {
            processData(fetchedData);
        }

        //Process the retrieved data
        private void processData(List<DataEntry> fetchedData) {
            if (fetchedData != null) {
                View rootView = getView();

                if (rootView != null) {
                    LineChart lineChart = rootView.findViewById(R.id.line_chart);
                    lineChart.setBackgroundColor(Color.TRANSPARENT);
                    lineChart.setNoDataText("");
                    ArrayList<Entry> entriesValue1 = new ArrayList<>();
                    ArrayList<Entry> entriesValue2 = new ArrayList<>();
                    ArrayList<Entry> entriesValue3 = new ArrayList<>();
                    ArrayList<Entry> entriesValue4 = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();
                    for (int i = 0; i < fetchedData.size(); i++) {
                        DataEntry entry = fetchedData.get(i);
                        entriesValue1.add(new Entry(i, entry.getValue1()));
                        entriesValue2.add(new Entry(i, entry.getValue2()));
                        entriesValue3.add(new Entry(i, entry.getValue3()));
                        entriesValue4.add(new Entry(i, entry.getValue4()));
                        labels.add(entry.getDate());
                    }

                    LineDataSet dataSetValue1 = new LineDataSet(entriesValue1, "Value at 7 am");
                    dataSetValue1.setColor(getResources().getColor(R.color.black));

                    LineDataSet dataSetValue2 = new LineDataSet(entriesValue2, "Value at 1 pm");
                    dataSetValue2.setColor(getResources().getColor(R.color.purple_700));

                    LineDataSet dataSetValue3 = new LineDataSet(entriesValue3, "Value at 7 pm");
                    dataSetValue3.setColor(getResources().getColor(R.color.teal_200));

                    LineDataSet dataSetValue4 = new LineDataSet(entriesValue4, "Value before bedtime");
                    dataSetValue4.setColor(getResources().getColor(R.color.teal_700));

                    LineData lineData = new LineData(dataSetValue1, dataSetValue2, dataSetValue3, dataSetValue4);
                    lineChart.setData(lineData);

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

                    lineChart.invalidate();

                    StringBuilder message = new StringBuilder();
                    for (DataEntry entry : fetchedData) {
                        message.append("Date: ").append(entry.getDate()).append("\n");
                        message.append("Value at 7 am: ").append(entry.getValue1()).append("\n");
                        message.append("Value at 1 pm: ").append(entry.getValue2()).append("\n");
                        message.append("Value at 7 pm: ").append(entry.getValue3()).append("\n");
                        message.append("Value before bedtime: ").append(entry.getValue4()).append("\n\n");
                    }
                    addToArray(message.toString());
                } else {
                    showToast("No data received");
                }
            }
        }

    }

        private void addToArray(String message) {
        ArrayToDoc.add(message);
    }


    private void sendEmail() {
        String subject = "Information about patient " + loginInfo.split("-")[0];
        StringBuilder body = new StringBuilder();
        for (String entry : ArrayToDoc) {
            body.append(entry).append("\n");
        }
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        String email = loginInfo.split("-")[2];
        String encodedSubject = Uri.encode("Information about patient " + loginInfo.split("-")[0]);
        String encodedBody = Uri.encode(body.toString());
        String UriText = "mailto:" + email + "?subject=" + encodedSubject + "&body=" + encodedBody;
        emailIntent.setData(Uri.parse(UriText));

        if (emailIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        }
    }



    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


    // Class that holds data for each date
    private static class DataEntry {
        private String date;
        private int value1;
        private int value2;
        private int value3;
        private int value4;

        public DataEntry(String date, int value1, int value2, int value3, int value4) {
            this.date = date;
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
            this.value4 = value4;
        }

        public String getDate() {
            return date;
        }

        public int getValue1() {
            return value1;
        }

        public int getValue2() {
            return value2;
        }

        public int getValue3() {
            return value3;
        }

        public int getValue4() {
            return value4;
        }
    }
}