package com.ngocsang.smscode;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        this.mListener.onComplete(year, month, day);
    }
    public static interface OnCompleteListener {
        public abstract void onComplete(int year, int month, int day);
    }

    private OnCompleteListener mListener;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }
}