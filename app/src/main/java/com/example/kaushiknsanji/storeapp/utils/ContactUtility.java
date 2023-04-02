/*
 * Copyright 2018 Kaushik N. Sanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kaushiknsanji.storeapp.utils;

import android.support.v4.util.PatternsCompat;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Patterns;


public final class ContactUtility {

    //Constants for the Minimum and Maximum length of Phone Number
    //(as per the standards read from Internet at the time of writing)
    private static final int PHONE_NUMBER_MIN_LENGTH = 4;
    private static final int PHONE_NUMBER_MAX_LENGTH = 15;

    //Constant for the Maximum length of Email
    //(as per the standards read from Internet at the time of writing)
    private static final int EMAIL_MAX_LENGTH = 254;

    //Constant for the separator in email address
    private static final String EMAIL_LOCAL_DOMAIN_SEPARATOR = "@";


    private ContactUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            //Returning FALSE when the Email address is empty or NULL
            return false;
        }

        if (email.length() > EMAIL_MAX_LENGTH) {
            //Returning FALSE when the Email address exceeds 254 alphanumeric characters
            return false;
        }

        if (!email.contains(EMAIL_LOCAL_DOMAIN_SEPARATOR)) {
            //Returning FALSE when the Email address does not contain "@" separator
            return false;
        }

        //Finally, evaluating by matching the email address with PatternsCompat#EMAIL_ADDRESS
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        //Translating the Phone Number into actual digits prior to evaluating it
        phoneNumber = convertAndStripPhoneNumber(phoneNumber);

        if (TextUtils.isEmpty(phoneNumber)
                || phoneNumber.length() < PHONE_NUMBER_MIN_LENGTH
                || phoneNumber.length() > PHONE_NUMBER_MAX_LENGTH) {
            //Returning FALSE when the length of Phone Number is not within the bounds (4 - 15 digits)
            return false;
        }

        //Finally, evaluating by matching the Phone Number with Patterns#PHONE
        return Patterns.PHONE.matcher(phoneNumber).matches() & PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber);
    }


    public static String convertAndStripPhoneNumber(String phoneNumber) {
        //Converting to actual digits and removing all separators
        return PhoneNumberUtils.stripSeparators(PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber));
    }

    public static int getPhoneNumberMaxLength() {
        return PHONE_NUMBER_MAX_LENGTH;
    }

    public static int getPhoneNumberMinLength() {
        return PHONE_NUMBER_MIN_LENGTH;
    }


    public static int getEmailMaxLength() {
        return EMAIL_MAX_LENGTH;
    }
}
