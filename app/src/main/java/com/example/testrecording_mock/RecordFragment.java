package com.example.testrecording_mock;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.Image;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment implements View.OnClickListener {

    private NavController navController;
    private ImageButton rcdListBtn;
    private ImageButton rcdBtn;
    private TextView filenameText;

    private boolean isRecording = false;
    private int PERMISSION_CODE = 21;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer timer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        rcdListBtn = view.findViewById(R.id.record_list_btn);
        rcdBtn = view.findViewById(R.id.record_btn);
        timer = view.findViewById(R.id.record_timer);
        filenameText = view.findViewById(R.id.record_filename);

        rcdListBtn.setOnClickListener(this);
        rcdBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_list_btn:
                if(isRecording){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                            isRecording = false;
                        }
                    });
                    alertDialog.setNegativeButton("CANCEL", null);
                    alertDialog.setTitle("Audio Still Recording");
                    alertDialog.setMessage("Are you sure, you want to stop recording?");
                    alertDialog.create().show();
                } else {
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                }
                break;

            case R.id.record_btn:
                if(isRecording){
                    //Stop Recording
                    stopRecording();

                    rcdBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn,  null));
                    isRecording = false;
                }else{
                    if(checkPermissions()) {
                        //Start Recording
                        startRecording();

                        rcdBtn.setImageDrawable(getResources().getDrawable(R.drawable.grey_sqr, null));
                        isRecording = true;
                    }
                }
                break;
        }
    }

    private void stopRecording() {
        timer.stop();

        filenameText.setText("Record Stopped, File Saved : " + recordFile);

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private void startRecording() {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.TAIWAN);
        Date now = new Date();

        recordFile = "Recording..." + formatter.format(now) + ".gp3";

        filenameText.setText("Recording, File Name : " + recordFile);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    private boolean checkPermissions() {
        if(ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording) {
            stopRecording();
        }
    }
}
