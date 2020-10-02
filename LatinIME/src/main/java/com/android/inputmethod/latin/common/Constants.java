/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin.common;

import android.util.Log;

import com.android.inputmethod.annotations.UsedForTesting;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.annotation.Nonnull;

public final class Constants {

    public static final class Color {
        /**
         * The alpha value for fully opaque.
         */
        public final static int ALPHA_OPAQUE = 255;
    }

    public static final class ImeOption {
        /**
         * The private IME option used to indicate that no microphone should be shown for a given
         * text field. For instance, this is specified by the search dialog when the dialog is
         * already showing a voice search button.
         *
         * @deprecated Use {@link ImeOption#NO_MICROPHONE} with package name prefixed.
         */
        @SuppressWarnings("dep-ann")
        public static final String NO_MICROPHONE_COMPAT = "nm";

        /**
         * The private IME option used to indicate that no microphone should be shown for a given
         * text field. For instance, this is specified by the search dialog when the dialog is
         * already showing a voice search button.
         */
        public static final String NO_MICROPHONE = "noMicrophoneKey";

        /**
         * The private IME option used to indicate that no settings key should be shown for a given
         * text field.
         */
        public static final String NO_SETTINGS_KEY = "noSettingsKey";

        /**
         * The private IME option used to indicate that the given text field needs ASCII code points
         * input.
         *
         * @deprecated Use EditorInfo#IME_FLAG_FORCE_ASCII.
         */
        @SuppressWarnings("dep-ann")
        public static final String FORCE_ASCII = "forceAscii";

        /**
         * The private IME option used to suppress the floating gesture preview for a given text
         * field. This overrides the corresponding keyboard settings preference.
         * {@link com.android.inputmethod.latin.settings.SettingsValues#mGestureFloatingPreviewTextEnabled}
         */
        public static final String NO_FLOATING_GESTURE_PREVIEW = "noGestureFloatingPreview";

        private ImeOption() {
            // This utility class is not publicly instantiable.
        }
    }

    public static final class Subtype {
        /**
         * The subtype mode used to indicate that the subtype is a keyboard.
         */
        public static final String KEYBOARD_MODE = "keyboard";

        public static final class ExtraValue {
            /**
             * The subtype extra value used to indicate that this subtype is capable of
             * entering ASCII characters.
             */
            public static final String ASCII_CAPABLE = "AsciiCapable";

            /**
             * The subtype extra value used to indicate that this subtype is enabled
             * when the default subtype is not marked as ascii capable.
             */
            public static final String ENABLED_WHEN_DEFAULT_IS_NOT_ASCII_CAPABLE =
                    "EnabledWhenDefaultIsNotAsciiCapable";

            /**
             * The subtype extra value used to indicate that this subtype is capable of
             * entering emoji characters.
             */
            public static final String EMOJI_CAPABLE = "EmojiCapable";

            /**
             * The subtype extra value used to indicate that this subtype requires a network
             * connection to work.
             */
            public static final String REQ_NETWORK_CONNECTIVITY = "requireNetworkConnectivity";

            /**
             * The subtype extra value used to indicate that the display name of this subtype
             * contains a "%s" for printf-like replacement and it should be replaced by
             * this extra value.
             * This extra value is supported on JellyBean and later.
             */
            public static final String UNTRANSLATABLE_STRING_IN_SUBTYPE_NAME =
                    "UntranslatableReplacementStringInSubtypeName";

            /**
             * The subtype extra value used to indicate this subtype keyboard layout set name.
             * This extra value is private to LatinIME.
             */
            public static final String KEYBOARD_LAYOUT_SET = "KeyboardLayoutSet";

            /**
             * The subtype extra value used to indicate that this subtype is an additional subtype
             * that the user defined. This extra value is private to LatinIME.
             */
            public static final String IS_ADDITIONAL_SUBTYPE = "isAdditionalSubtype";

            /**
             * The subtype extra value used to specify the combining rules.
             */
            public static final String COMBINING_RULES = "CombiningRules";

            //AnhNDd: them gia tri COMBINING_RULES
            public static final String COMBINING_RULES_VI = "vi";

            private ExtraValue() {
                // This utility class is not publicly instantiable.
            }
        }

        private Subtype() {
            // This utility class is not publicly instantiable.
        }
    }

    public static final class TextUtils {
        /**
         * Capitalization mode for {@link android.text.TextUtils#getCapsMode}: don't capitalize
         * characters.  This value may be used with
         * {@link android.text.TextUtils#CAP_MODE_CHARACTERS},
         * {@link android.text.TextUtils#CAP_MODE_WORDS}, and
         * {@link android.text.TextUtils#CAP_MODE_SENTENCES}.
         */
        // TODO: Straighten this out. It's bizarre to have to use android.text.TextUtils.CAP_MODE_*
        // except for OFF that is in Constants.TextUtils.
        public static final int CAP_MODE_OFF = 0;

        private TextUtils() {
            // This utility class is not publicly instantiable.
        }
    }

    public static final int NOT_A_CODE = -1;
    public static final int NOT_A_CURSOR_POSITION = -1;
    // TODO: replace the following constants with state in InputTransaction?
    public static final int NOT_A_COORDINATE = -1;
    public static final int SUGGESTION_STRIP_COORDINATE = -2;
    public static final int EXTERNAL_KEYBOARD_COORDINATE = -4;

    // A hint on how many characters to cache from the TextView. A good value of this is given by
    // how many characters we need to be able to almost always find the caps mode.
    public static final int EDITOR_CONTENTS_CACHE_SIZE = 1024;
    // How many characters we accept for the recapitalization functionality. This needs to be
    // large enough for all reasonable purposes, but avoid purposeful attacks. 100k sounds about
    // right for this.
    public static final int MAX_CHARACTERS_FOR_RECAPITALIZATION = 1024 * 100;

    // Key events coming any faster than this are long-presses.
    public static final int LONG_PRESS_MILLISECONDS = 500; //AnhNDd 200->500
    // TODO: Set this value appropriately.
    public static final int GET_SUGGESTED_WORDS_TIMEOUT = 200;
    // How many continuous deletes at which to start deleting at a higher speed.
    public static final int DELETE_ACCELERATE_AT = 20;

    public static final String WORD_SEPARATOR = " ";

    public static boolean isValidCoordinate(final int coordinate) {
        // Detect {@link NOT_A_COORDINATE}, {@link SUGGESTION_STRIP_COORDINATE},
        // and {@link SPELL_CHECKER_COORDINATE}.
        return coordinate >= 0;
    }

    /**
     * Custom request code used in
     * {@link com.android.inputmethod.keyboard.KeyboardActionListener#onCustomRequest(int)}.
     */
    // The code to show input method picker.
    public static final int CUSTOM_CODE_SHOW_INPUT_METHOD_PICKER = 1;

    /**
     * Some common keys code. Must be positive.
     */
    public static final int CODE_ENTER = '\n';
    public static final int CODE_TAB = '\t';
    public static final int CODE_SPACE = ' ';
    public static final int CODE_PERIOD = '.';
    public static final int CODE_COMMA = ',';
    public static final int CODE_DASH = '-';
    public static final int CODE_SINGLE_QUOTE = '\'';
    public static final int CODE_DOUBLE_QUOTE = '"';
    public static final int CODE_SLASH = '/';
    public static final int CODE_BACKSLASH = '\\';
    public static final int CODE_VERTICAL_BAR = '|';
    public static final int CODE_COMMERCIAL_AT = '@';
    public static final int CODE_PLUS = '+';
    public static final int CODE_PERCENT = '%';
    public static final int CODE_CLOSING_PARENTHESIS = ')';
    public static final int CODE_CLOSING_SQUARE_BRACKET = ']';
    public static final int CODE_CLOSING_CURLY_BRACKET = '}';
    public static final int CODE_CLOSING_ANGLE_BRACKET = '>';
    public static final int CODE_INVERTED_QUESTION_MARK = 0xBF; // ¿
    public static final int CODE_INVERTED_EXCLAMATION_MARK = 0xA1; // ¡
    public static final int CODE_GRAVE_ACCENT = '`';
    public static final int CODE_CIRCUMFLEX_ACCENT = '^';
    public static final int CODE_TILDE = '~';

    public static final String REGEXP_PERIOD = "\\.";
    public static final String STRING_SPACE = " ";

    /**
     * Special keys code. Must be negative.
     * These should be aligned with constants in
     * {@link com.android.inputmethod.keyboard.internal.KeyboardCodesSet}.
     */
    public static final int CODE_SHIFT = -1;
    public static final int CODE_CAPSLOCK = -2;
    public static final int CODE_SWITCH_ALPHA_SYMBOL = -3;
    public static final int CODE_OUTPUT_TEXT = -4;
    public static final int CODE_DELETE = -5;
    public static final int CODE_SETTINGS = -6;
    public static final int CODE_SHORTCUT = -7;
    public static final int CODE_ACTION_NEXT = -8;
    public static final int CODE_ACTION_PREVIOUS = -9;
    public static final int CODE_LANGUAGE_SWITCH = -10;
    public static final int CODE_EMOJI = -11;
    public static final int CODE_SHIFT_ENTER = -12;
    public static final int CODE_SYMBOL_SHIFT = -13;
    public static final int CODE_ALPHA_FROM_EMOJI = -14;
    // Code value representing the code is not specified.
    public static final int CODE_UNSPECIFIED = -15;

    public static boolean isLetterCode(final int code) {
        return code >= CODE_SPACE;
    }

    @Nonnull
    public static String printableCode(final int code) {
        switch (code) {
        case CODE_SHIFT: return "shift";
        case CODE_CAPSLOCK: return "capslock";
        case CODE_SWITCH_ALPHA_SYMBOL: return "symbol";
        case CODE_OUTPUT_TEXT: return "text";
        case CODE_DELETE: return "delete";
        case CODE_SETTINGS: return "settings";
        case CODE_SHORTCUT: return "shortcut";
        case CODE_ACTION_NEXT: return "actionNext";
        case CODE_ACTION_PREVIOUS: return "actionPrevious";
        case CODE_LANGUAGE_SWITCH: return "languageSwitch";
        case CODE_EMOJI: return "emoji";
        case CODE_SHIFT_ENTER: return "shiftEnter";
        case CODE_ALPHA_FROM_EMOJI: return "alpha";
        case CODE_UNSPECIFIED: return "unspec";
        case CODE_TAB: return "tab";
        case CODE_ENTER: return "enter";
        case CODE_SPACE: return "space";
        default:
            if (code < CODE_SPACE) return String.format("\\u%02X", code);
            if (code < 0x100) return String.format("%c", code);
            if (code < 0x10000) return String.format("\\u%04X", code);
            return String.format("\\U%05X", code);
        }
    }

    @Nonnull
    public static String printableCodes(@Nonnull final int[] codes) {
        final StringBuilder sb = new StringBuilder();
        boolean addDelimiter = false;
        for (final int code : codes) {
            if (code == NOT_A_CODE) break;
            if (addDelimiter) sb.append(", ");
            sb.append(printableCode(code));
            addDelimiter = true;
        }
        return "[" + sb + "]";
    }

    /**
     * Screen metrics (a.k.a. Device form factor) constants of
     * {@link com.android.inputmethod.latin.R.integer#config_screen_metrics}.
     */
    public static final int SCREEN_METRICS_SMALL_PHONE = 0;
    public static final int SCREEN_METRICS_LARGE_PHONE = 1;
    public static final int SCREEN_METRICS_LARGE_TABLET = 2;
    public static final int SCREEN_METRICS_SMALL_TABLET = 3;

    @UsedForTesting
    public static boolean isPhone(final int screenMetrics) {
        return screenMetrics == SCREEN_METRICS_SMALL_PHONE
                || screenMetrics == SCREEN_METRICS_LARGE_PHONE;
    }

    @UsedForTesting
    public static boolean isTablet(final int screenMetrics) {
        return screenMetrics == SCREEN_METRICS_SMALL_TABLET
                || screenMetrics == SCREEN_METRICS_LARGE_TABLET;
    }

    /**
     * Default capacity of gesture points container.
     * This constant is used by {@link com.android.inputmethod.keyboard.internal.BatchInputArbiter}
     * and etc. to preallocate regions that contain gesture event points.
     */
    public static final int DEFAULT_GESTURE_POINTS_CAPACITY = 128;

    public static final int MAX_IME_DECODER_RESULTS = 20;
    public static final int DECODER_SCORE_SCALAR = 1000000;
    public static final int DECODER_MAX_SCORE = 1000000000;

    public static final int EVENT_BACKSPACE = 1;
    public static final int EVENT_REJECTION = 2;
    public static final int EVENT_REVERT = 3;

    private Constants() {
        // This utility class is not publicly instantiable.
    }

    //=========================BKAV===========================
    //AnhNDd:
    public static final int CODE_CURSOR = -16;

    //AnhNDd:Thêm một số trường hợp để bàn phím xử lý.
    public static final int CODE_CTRL_A = -17;

    public static final int CODE_CTRL_C = -18;

    public static final int CODE_CTRL_V = -19;

    public static final int CODE_CTRL_X = -20;

    public static final int CODE_HIDE_WINDOW = -21;

    public static final int CODE_VOICE = -22;

    public static final String DICT_VI_TELEX_AUTOCORRECT_NAME = "main_vi_autocorrect.dict";

    //AnhNDd: để xác định việc từ vừa thêm vào từ điển là từ tự động sửa đúng hay sai
    public static final int FLAG_AUTOCORRECT_RIGHT = 0x20;

    public static final int FLAG_AUTOCORRECT_WRONG = 0x40;

    public static final char CODE_SPERATOR = '-';

    private static final int[] LIST_CHARACTER = {
            0X0041, 0X0042, 0X0043, 0X0044, 0X0045, 0X0046, 0X0047, 0X0048, 0X0049, 0X004A, 0X004B,
            0X004C, 0X004D, 0X004E, 0X004F, 0X0050, 0X0051, 0X0052, 0X0053, 0X0054, 0X0055, 0X0056,
            0X0057, 0X0058, 0X0059, 0X005A, 0X0061, 0X0062, 0X0063, 0X0064, 0X0065, 0X0066, 0X0067,
            0X0068, 0X0069, 0X006A, 0X006B, 0X006C, 0X006D, 0X006E, 0X006F, 0X0070, 0X0071, 0X0072,
            0X0073, 0X0074, 0X0075, 0X0076, 0X0077, 0X0078, 0X0079, 0X007A, 0X00C0, 0X00C1, 0X00C2,
            0X00C3, 0X00C8, 0X00C9, 0X00CA, 0X00CC, 0X00CD, 0X00D2, 0X00D3, 0X00D4, 0X00D5, 0X00D9,
            0X00DA, 0X00DD, 0X00E0, 0X00E1, 0X00E2, 0X00E3, 0X00E8, 0X00E9, 0X00EA, 0X00EC, 0X00ED,
            0X00F2, 0X00F3, 0X00F4, 0X00F5, 0X00F9, 0X00FA, 0X00FD, 0X0102, 0X0103, 0X0110, 0X0111,
            0X0128, 0X0129, 0X0168, 0X0169, 0X01A0, 0X01A1, 0X01AF, 0X01B0, 0X1EA0, 0X1EA1, 0X1EA2,
            0X1EA3, 0X1EA4, 0X1EA5, 0X1EA6, 0X1EA7, 0X1EA8, 0X1EA9, 0X1EAA, 0X1EAB, 0X1EAC, 0X1EAD,
            0X1EAE, 0X1EAF, 0X1EB0, 0X1EB1, 0X1EB2, 0X1EB3, 0X1EB4, 0X1EB5, 0X1EB6, 0X1EB7, 0X1EB8,
            0X1EB9, 0X1EBA, 0X1EBB, 0X1EBC, 0X1EBD, 0X1EBE, 0X1EBF, 0X1EC0, 0X1EC1, 0X1EC2, 0X1EC3,
            0X1EC4, 0X1EC5, 0X1EC6, 0X1EC7, 0X1EC8, 0X1EC9, 0X1ECA, 0X1ECB, 0X1ECC, 0X1ECD, 0X1ECE,
            0X1ECF, 0X1ED0, 0X1ED1, 0X1ED2, 0X1ED3, 0X1ED4, 0X1ED5, 0X1ED6, 0X1ED7, 0X1ED8, 0X1ED9,
            0X1EDA, 0X1EDB, 0X1EDC, 0X1EDD, 0X1EDE, 0X1EDF, 0X1EE0, 0X1EE1, 0X1EE2, 0X1EE3, 0X1EE4,
            0X1EE5, 0X1EE6, 0X1EE7, 0X1EE8, 0X1EE9, 0X1EEA, 0X1EEB, 0X1EEC, 0X1EED, 0X1EEE, 0X1EEF,
            0X1EF0, 0X1EF1, 0X1EF2, 0X1EF3, 0X1EF4, 0X1EF5, 0X1EF6, 0X1EF7, 0X1EF8, 0X1EF9
    };

    private static final int[] LIST_NUMBER = {
            0X0030, 0X0031, 0X0032, 0X0033, 0X0034, 0X0035, 0X0036, 0X0037, 0X0038, 0X0039
    };

    /**
     * AnhNDd: Check codepoint là codepoint tiếng việt
     *
     * @param codePoint
     * @return boolean
     */

    public static boolean isCodeVN(final int codePoint) {
        return Arrays.binarySearch(LIST_CHARACTER, codePoint) >= 0;
    }

    public static boolean isNumber(final int codePoint) {
        return Arrays.binarySearch(LIST_NUMBER, codePoint) >= 0;
    }

    //Bkav AnhNDd check thiết bị là máy của BCY
    public static boolean isBCY() {
        return "bcy".equals(getSystemProperty("persist.sys.bkav.partner", ""));
    }

    public static String getSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!android.text.TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            Log.d("Bkav", "Unable to read system properties");
        }
        return defaultValue;
    }
}
