package com.dlsu.getbetter.getbetter.cryptoGB;

/**
 * Created by User on 10/26/2017.
 */

public interface TaskListener {
    void onTaskStarted();
    void onTaskFinished(String result);
}
