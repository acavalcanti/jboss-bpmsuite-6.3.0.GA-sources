package com.redhat.installer.framework.mock;

import com.izforge.izpack.util.AbstractUIProcessHandler;

/**
 * Created by thauser on 2/4/14.
 */
public class MockAbstractUIProcessHandler implements AbstractUIProcessHandler {
    @Override
    public void logOutput(String message, boolean stderr) {
        if (!stderr){
            System.out.println(message);
        } else {
            System.err.println(message);
        }
    }

    @Override
    public void startProcessing(int no_of_processes) {

    }

    @Override
    public void startProcess(String name) {

    }

    @Override
    public void finishProcess() {

    }

    @Override
    public void finishProcessing(boolean unlockPrev, boolean unlockNext) {

    }

    @Override
    public void emitNotification(String message) {
        System.out.println(message);
    }

    @Override
    public boolean emitWarning(String title, String message) {
        return false;
    }

    @Override
    public void emitError(String title, String message) {

    }

    @Override
    public void emitErrorAndBlockNext(String title, String message) {

    }

    @Override
    public int askQuestion(String title, String question, int choices) {
        return 0;
    }

    @Override
    public int askQuestion(String title, String question, int choices, int default_choice) {
        return 0;
    }
}
