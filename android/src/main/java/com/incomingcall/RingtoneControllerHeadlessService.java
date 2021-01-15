package com.incomingcall;

import android.content.Intent;
import android.os.Bundle;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import javax.annotation.Nullable;

public class RingtoneControllerHeadlessService extends HeadlessJsTaskService {
    private static final long TIMEOUT_DEFAULT = 5000;
    private static final String TASK_KEY = "RingtoneControllerHeadlessTask";

    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) return null;
        return new HeadlessJsTaskConfig(
                TASK_KEY,
                Arguments.fromBundle(extras),
                TIMEOUT_DEFAULT,
                true
        );
    }
}
