package com.chatbot.nam.vietnamesechatbotlibrary.mainfeatures;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import static com.chatbot.nam.vietnamesechatbotlibrary.constant.LogTag.LOG_TAG;

public abstract class SpeechRecognitionService extends Service implements RecognitionListener {

    private HashMap<Integer, SpeechRecognizer> speechRecognizerStack = new HashMap<>();

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;

    private boolean isBotListening = true;

    private boolean isCreatingService = true;

    private TextAnalysisThread textAnalysisThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!isCreatingService) {
            speechRecognizer.startListening(recognizerIntent);
        }

        Toast.makeText(getApplicationContext(), "Start Service", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Log.d(LOG_TAG, speechRecognizer.hashCode() + "");
        speechRecognizer.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        isCreatingService = false;

    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "Bind", Toast.LENGTH_SHORT)
                .show();
        return null;
    }

    @Override
    public void onDestroy() {

        Toast.makeText(getApplicationContext(), "Destroy", Toast.LENGTH_SHORT)
                .show();
        super.onDestroy();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(LOG_TAG, "ready for speech");
//		Toast.makeText(getApplicationContext(), "Ready for Listening",
//				Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(LOG_TAG, "begin of speech");
//		Toast.makeText(getApplicationContext(), "Start Recording",
//				Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        // Intent intent = new Intent(getApplicationContext(),
        // CustomProgressDialog.class);
        // startActivity(intent);
        Log.d(LOG_TAG, "End of speech");
    }

    @Override
    public void onError(int error) {
        String errorText = getErrorText(error);
        Log.d(LOG_TAG, "Speech Error: " + error);
        Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT)
                .show();

    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String command = matches.get(0);

        if (textAnalysisThread == null) textAnalysisThread = getTextAnalysisThread();
        textAnalysisThread.setInputMessage(command);
        textAnalysisThread.execute();

        Toast.makeText(getApplicationContext(), command, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> partials = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (String partial : partials) {
            Log.d(LOG_TAG, "Partial: " + partial);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d(LOG_TAG, "On Event: " + eventType);
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public abstract TextAnalysisThread getTextAnalysisThread();

}
