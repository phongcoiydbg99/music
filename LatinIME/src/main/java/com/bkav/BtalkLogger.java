
package com.bkav;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.zip.ZipOutputStream;

public class BtalkLogger {

    public static final boolean DEBUG = true;

    public static final boolean DEBUG_LOAD_VIEW = false; // debug load cham build can gan lai fasle

    public static final boolean DEBUG_ENABLE_TOAST = DEBUG;

    public static final boolean DEBUG_RAM = DEBUG;

    public static final boolean DEBUG_LOG = DEBUG;

    public static final boolean DEBUG_PERFORMANCE_AVATAR = false;

    public static final boolean DEBUG_ONLINE = DEBUG;

    public static final boolean DEBUG_SEARCH_PHONE = DEBUG;

    public static final boolean DEBUG_TIME = DEBUG_LOAD_VIEW;

    public static final boolean DEBUG_ANDROID = DEBUG;

    public static final boolean DEBUG_CLOSE = false;

    public static final boolean DEBUG_USE_TEMP_FUNCTIONS = false;

    public static final boolean DEBUG_LOAD_TIME = DEBUG;

    public static final boolean DEBUG_TASK_CONTROLLER = DEBUG;

    public static final boolean DEBUG_CONNECTION = DEBUG;

    public static final boolean DEBUG_INIT_BUDDIES = false;

    public static final boolean DEBUG_FILE_TRANSFER = DEBUG; // QuyetDV bat co = true de debug phan gui file

    public static final boolean DEBUG_OFFLINE_MESSAGE = false;

    private static final boolean SAVE_LOG_TO_FILE = DEBUG;

    public static final String LOG_FOLDER_PATH;

    private LogFile mSmackLogFile;

    private LogFile mLinphoneLogFile;

    private LogFile mGeneralLogFile;

    private LogFile mConnectionLogFile;

    private LogFile mGoogleAnalyticsLogFile;

    private LogcatMonitor mLogcatMonitor;

    // QuyetDV: Lop ghi log thoi gian truoc khi upload file, DBCL test xong thi xoa
    private LogFile mUploadTimeLogFile;

    private Handler mPrivateHandler;

    private static final int TOAST_MSG = 1;

    static {
        File file = Environment.getExternalStorageDirectory();
        file = new File(file.getAbsolutePath() + "/Bkav Corporation/GTV/Log");
        if (!file.exists()) {
            file.mkdirs();
        }

        LOG_FOLDER_PATH = file.getAbsolutePath();
    }

    public static final String TAG = "BkavGTV";

    private enum Type {
        ERROR, DEBUG, WARNING, INFO
    }

    private BtalkLogger() {
        if (!DEBUG) {
            return;
        }

        mGeneralLogFile = new LogFile("general");
        mConnectionLogFile = new LogFile("connection");
        mGoogleAnalyticsLogFile = new LogFile("ga");
        mLinphoneLogFile = new LogFile("debug_linphone");
        mSmackLogFile = new LogFile("smack");
        mLogcatMonitor = new LogcatMonitor();

        mUploadTimeLogFile = new LogFile("upload");

        if (SAVE_LOG_TO_FILE) {
            new Thread(mLogcatMonitor).start();
        }

        mPrivateHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == TOAST_MSG) {
                    //Context context = MmsApp.getApplication();
                    Context context = null;
                    if (context != null) {
                        Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    }
                }
            };
        };
    }

    //@edu.umd.cs.findbugs.annotations.SuppressWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    private static BtalkLogger sInstance;

    public static BtalkLogger getInstance() {
        if (sInstance == null) {
            sInstance = new BtalkLogger();
        }

        return sInstance;
    }

    public void destroy() {
        sInstance = null;

        if (!DEBUG) {
            return;
        }

        try {
            mGoogleAnalyticsLogFile.close();
            mGeneralLogFile.close();
            mConnectionLogFile.close();
            mLogcatMonitor.stop();
            mSmackLogFile.close();
            mUploadTimeLogFile.close();

        } catch (IOException e) {
        }
    }

    public void logLinphone() {

    }

    public void logGeneral(String message) {
        log(Type.DEBUG, DEBUG, "General: " + message);
    }

    public void logLoadTime(String message) {
        log(Type.DEBUG, DEBUG_LOAD_TIME, "Load time: " + message);
    }

    public void logInitBuddies(String message) {
        log(Type.DEBUG, DEBUG_INIT_BUDDIES, "Init buddies: " + message);
    }

    public void logTaskController(String message) {
        log(Type.DEBUG, DEBUG_TASK_CONTROLLER, "Task controller: " + message);
    }

    public void logGoogleAnalytics(String message) {
        // Bkav QuangLH: neu khong cau hinh log thi return
        if (!DEBUG) {
            return;
        }

        log(Type.DEBUG, DEBUG, "Google analytics: " + message);
        mGoogleAnalyticsLogFile.saveLogToFile(message);
    }

    public void logFileTransfer(String message) {
        log(Type.DEBUG, DEBUG_FILE_TRANSFER, "File transfer: " + message);
    }

    public void logOfflineMessage(String message) {
        log(Type.DEBUG, DEBUG_OFFLINE_MESSAGE, "Offline message: " + message);
    }

    private void log(Type type, boolean enableLog, String message) {
        // Bkav QuangLH: neu khong cau hinh log thi return
        if (!DEBUG) {
            return;
        }

        if (enableLog) {
            if (type == Type.DEBUG) {
                Log.v(TAG, message);
            } else if (type == Type.ERROR) {
                Log.e(TAG, message);
            }
        }

        mGeneralLogFile.saveLogToFile(message);
    }

    /*public boolean isRestartService() {
        if (mGoogleAnalyticsLogFile != null) {
            return mGoogleAnalyticsLogFile.isRestartService();
        }
        return true;
    }*/

    public static void debugUiHangConversations(String message) {
        if (DEBUG_LOG)
            Log.w("BtalkLoggerDebug", "Conversation: " + message);
    }

    public static void debugUiHangRecents(String message) {
        if (DEBUG_LOG)
            Log.w("BtalkLoggerDebug", "Recents: " + message);
    }

    public static void debugUiHangContactsList(String message) {
        if (DEBUG_LOG)
            Log.w("BtalkLoggerDebug", "Contacts list: " + message);
    }

    public void toast(String message) {
        if (DEBUG_ENABLE_TOAST) {
            Message msg = mPrivateHandler.obtainMessage(TOAST_MSG);
            msg.obj = message;
            mPrivateHandler.sendMessage(msg);
        }
    }

    private class LogcatMonitor implements Runnable {

        private static final String SMACK_LOG_RCV = "RCV  (";

        private static final String SMACK_LOG_SENT = "SENT (";

        private static final String LINPHONE_LOG_QUANGLH = "QuangLH";

        private static final String LINPHONE_LOG_SERVICE_DIED = "has died.";

        private static final String LINPHONE_LOG_MESSAGE_SENT = "]: message sent to [";

        private static final String LINPHONE_LOG_RECEIVED_MESSAGE = "]: received [";

        private boolean mKeepRunning;

        public LogcatMonitor() {
            mKeepRunning = true;
        }

        public void stop() {
            mKeepRunning = false;
        }

        @Override
        public void run() {
            try {
                while (mKeepRunning) {
                    collectLogs();
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
            } finally {

            }
        }

        private void collectLogs() {
            BufferedReader br = null;
            Process p = null;
            try {
                //p = Runtime.getRuntime().exec("logcat -v time Btalk:* *:S");
                p = Runtime.getRuntime().exec("logcat -v time");
                br = new BufferedReader(new InputStreamReader(p.getInputStream()), 2048);

                String line;
                boolean savingToFile = false;
                String oldTimeTag = "";

                while ((line = br.readLine()) != null && mKeepRunning) {
                    if (line.contains(SMACK_LOG_RCV) || line.contains(SMACK_LOG_SENT)) {
                        // Bkav QuangLH: log asmack
                        mSmackLogFile.saveLogToFile("\r\n" + line);
                    } else if (line.contains(LINPHONE_LOG_RECEIVED_MESSAGE)
                            || line.contains(LINPHONE_LOG_MESSAGE_SENT) || savingToFile) {
                        if (!savingToFile) {
                            savingToFile = true;
                            oldTimeTag = getTimeTag(line);
                            mLinphoneLogFile.saveLogToFile("=============== " + oldTimeTag
                                    + " =================");
                        }

                        if (shouldLog(oldTimeTag, getTimeTag(line))) {
                            mLinphoneLogFile.saveLogToFile(line);
                        } else {
                            savingToFile = false;
                        }
                    } else if (line.contains(LINPHONE_LOG_QUANGLH)
                            || line.contains(LINPHONE_LOG_SERVICE_DIED)) {
                        mLinphoneLogFile.saveLogToFile("\r\n" + line);
                    }
                }
            } catch (IOException e) {
                mLinphoneLogFile.saveLogToFile("Loi dong logcat: e = " + e);
            } finally {
                mLinphoneLogFile.saveLogToFile("Ket thuc doc logcat");
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        private String getTimeTag(String line) {
            int i = 0;
            i = line.indexOf(' ');
            if (i != -1) {
                i = line.indexOf(' ', i + 1);

                if (i != -1) {
                    return line.substring(0, i);
                }
            }

            return "";
        }

        /** <p>Bkav QuangLH: Mot log lien quan toi SIP message co the gom vai chuc
         * dong. baseTime la thoi diem bat dau log dong dau tien. currentTime la
         * thoi diem phut dong log hien tai. Ta check la neu currentTime van nam
         * trong khoang 100ms ke tu baseTime thi ghi lai log.
         * 
         * <p>baseTime va currentTime la ket qua tra ve cua ham getTimeTag va co 
         * dang "10-25 14:12:12.160" */
        private boolean shouldLog(String baseTime, String currentTime) {
            // Bkav QuangLH: khong kiem tra String, mac dinh la 2 String phai
            // khac null va co do dai bang nhau
            byte[] bytesBaseTime = baseTime.getBytes();
            byte[] bytesCurrentTime = currentTime.getBytes();

            int length = bytesBaseTime.length;
            if (length != bytesCurrentTime.length) {
                logGeneral("Khong luu log do do dai xau khac nhau: baseTime = " + baseTime
                        + "; currentTime = " + currentTime);
                return false;
            }

            int i = 0;
            while (i < length && bytesBaseTime[i] == bytesCurrentTime[i]) {
                i++;
            }

            // Bkav QuangLH: 2 xau giong het nhau hoac sai biet duoi 100ms
            if (i >= length - 2) {
                return true;
            }

            return false;
        }
    }

    private class LogFile {
        private long mLastDay;

        private BufferedWriter mBufferedWriter;

        private String mTag;

        public LogFile(String tag) {
            mTag = tag;
            mLastDay = 0;
        }

        public void close() throws IOException {
            if (mBufferedWriter != null) {
                mBufferedWriter.close();
            }
        }

        public void saveLogToFile(String message) {
            if (SAVE_LOG_TO_FILE) {
                try {
                    BufferedWriter out = getTodayLogOutput();

                    String log = getCurrentTimeString() + message + "\n";
                    out.write(log);
                    out.flush();
                } catch (IOException e) {
                }
            }
        }

        private BufferedWriter getTodayLogOutput() throws IOException {
            Calendar c = Calendar.getInstance();

            String month = getFullDate(String.valueOf(c.get(Calendar.MONTH) + 1));
            String day = getFullDate(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));

            StringBuilder fileName = new StringBuilder(TAG).append("_")
                    .append(c.get(Calendar.YEAR)).append("_").append(month).append("_").append(day)
                    .append("_").append(mTag).append(".log");

            if (mLastDay == 0 || mLastDay != c.get(Calendar.DAY_OF_MONTH)) {
                mLastDay = c.get(Calendar.DAY_OF_MONTH);

                // Bkav QuangLH: tao file general
                File file = new File(LOG_FOLDER_PATH + "/" + fileName.toString());

                if (!file.exists()) {
                    // Bkav QuangLH: zip va xoa file log cu
                    zipAllOldFiles();

                    file.createNewFile();
                    mBufferedWriter = null;
                }
            }

            if (mBufferedWriter == null) {
                mBufferedWriter = new BufferedWriter(new FileWriter(LOG_FOLDER_PATH + "/"
                        + fileName.toString(), true));
            }

            return mBufferedWriter;
        }

        private void zipAllOldFiles() {
            File logFolder = new File(LOG_FOLDER_PATH);
            File[] files = logFolder.listFiles();

            if (files == null)
                return;

            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".log") && fileName.contains(mTag)) {
                    try {
                        zip(file);
                        file.delete();
                    } catch (Exception e) {
                    }
                }
            }
        }

        private void zip(File zippedFile) throws Exception {
            Log.v("XZip", "ZipFiles(String, String, ZipOutputStream)");

            FileOutputStream dest = new FileOutputStream(zippedFile.getAbsoluteFile() + ".zip");
            ZipOutputStream zipOutputSteam = new ZipOutputStream(new BufferedOutputStream(dest));
            try {
                if (zippedFile.isFile()) {

                    java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(
                            zippedFile.getName());
                    java.io.FileInputStream inputStream = new java.io.FileInputStream(zippedFile);
                    zipOutputSteam.putNextEntry(zipEntry);

                    int len;
                    byte[] buffer = new byte[4096];

                    while ((len = inputStream.read(buffer)) != -1) {
                        zipOutputSteam.write(buffer, 0, len);
                    }

                    zipOutputSteam.closeEntry();
                    zipOutputSteam.flush();

                }
            } finally {
                if (zipOutputSteam != null) {
                    zipOutputSteam.close();
                }
                if (dest != null) {
                    dest.close();
                }
            }

        }

        private String getCurrentTimeString() {
            Calendar c = Calendar.getInstance();

            return new StringBuilder().append(c.get(Calendar.HOUR_OF_DAY)).append(":")
                    .append(c.get(Calendar.MINUTE)).append(":").append(c.get(Calendar.SECOND))
                    .append(" ").toString();
        }

        private String getFullDate(String value) {
            return (value.length() == 1) ? ("0" + value) : value;
        }

        private BufferedReader getTodayLogFile() throws IOException {
            Calendar c = Calendar.getInstance();

            String month = getFullDate(String.valueOf(c.get(Calendar.MONTH) + 1));
            String day = getFullDate(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));

            StringBuilder fileName = new StringBuilder(TAG).append("_")
                    .append(c.get(Calendar.YEAR)).append("_").append(month).append("_").append(day)
                    .append("_").append(mTag).append(".log");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(LOG_FOLDER_PATH + "/"
                    + fileName.toString()));
            return bufferedReader;
        }

        /*public boolean isRestartService() {
            ArrayList<String> mReasonError = new ArrayList<String>();
            boolean isRestartService = false;
            BufferedReader bufferedReader;
            try {
                bufferedReader = getTodayLogFile();
                if (bufferedReader == null) {
                    // Truong hop ko co loi gi trong file
                    isRestartService = true;
                }
                StringBuilder content = new StringBuilder();
                // Doc thong tin cua file 
                while (bufferedReader.ready()) {
                    String line = bufferedReader.readLine();
                    if (TextUtils.isEmpty(line)) {
                        if (!TextUtils.isEmpty(content.toString())) {
                            mReasonError.add(content.toString());
                            content = new StringBuilder();
                        }
                    } else {
                        if (line.contains("=====")
                                || (line.charAt(0) > '0' && line.charAt(0) <= '9')) {
                            continue;
                        }
                        content.append(line);
                    }
                }
                int size = mReasonError.size();
                if (size == 0 || size == 1) {
                    isRestartService = true;
                } else {
                    String newReasonError = mReasonError.get(size - 1);
                    String oldReasonError = mReasonError.get(size - 2);
                    if (newReasonError.equalsIgnoreCase(oldReasonError)) {
                        isRestartService = false;
                    } else {
                        isRestartService = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                isRestartService = true;
            }
            return isRestartService;
        }*/
    }

    public static long sTimeStartBackupFolder;

    // QuyetDV: Ham ghi log thoi gian truoc khi upload file, DBCL test xong thi bo di
    public void logLoadTimeUploadFile(String message) {
        logUploadFile(Type.DEBUG, DEBUG_LOAD_TIME, "Load time: " + message);
    }

    private void logUploadFile(Type type, boolean enableLog, String message) {
        if (!DEBUG) {
            return;
        }

        if (enableLog) {
            if (type == Type.DEBUG) {
                Log.v(TAG, message);
            } else if (type == Type.ERROR) {
                Log.e(TAG, message);
            }
        }

        mUploadTimeLogFile.saveLogToFile(message);
    }

}
