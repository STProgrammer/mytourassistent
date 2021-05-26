package com.aphex.mytourassistent.views.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.repository.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private FirebaseAuth mAuth;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            mAuth = FirebaseAuth.getInstance();


            EditTextPreference numberPreference = findPreference("nr_of_hours");
            SwitchPreferenceCompat switchPreferenceCompat = findPreference("auto_cancellation_checkbox");

            //Code to enable or disable number input if switch is checked
            if (switchPreferenceCompat.isChecked()) {
                numberPreference.setEnabled(true);
            } else numberPreference.setEnabled(false);

            switchPreferenceCompat.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue == true) {
                    numberPreference.setEnabled(true);
                } else {
                    numberPreference.setEnabled(false);
                }
                return true;
            });

            if (numberPreference != null) {
                numberPreference.setOnBindEditTextListener(
                        editText -> {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            editText.setFilters(new InputFilter[]{new InputFilterMinMax(getResources().getInteger(R.integer.pref_limit_minimum_hours), getResources().getInteger(R.integer.pref_limit_maximum_hours))});

                        });
            }


            Preference deleteUser = findPreference("delete_user");
            deleteUser.setOnPreferenceClickListener(preference -> {
                //show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle(R.string.delete_user_dialog_title);
                builder.setMessage(R.string.are_you_sure_to_delete);
                EditText editText = new EditText(requireActivity());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(editText);
                builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> deleteUserFromFirebase(editText.getText().toString()));
                builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
                builder.show();
                return false;
            });

        }

        private void deleteUserFromFirebase(String password) {

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                return;
            }

            AuthCredential credential = EmailAuthProvider
                    .getCredential(currentUser.getEmail(), password);

            //reauthenticate før sletting:
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                // Utfør sletting:
                doDeleteFromFirebase(currentUser);
            }).addOnFailureListener(e -> Toast.makeText(requireContext(), R.string.toast_wrong_password, Toast.LENGTH_SHORT).show());
        }

        // Utfører sletting:
        private void doDeleteFromFirebase(FirebaseUser user) {
            user.delete().addOnSuccessListener(aVoid -> {
                Repository repository = Repository.getInstance(requireActivity());
                repository.clearDatabase();
                Toast.makeText(requireActivity(), R.string.toast_user_deleted, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                requireActivity().finish();
            })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireActivity(), R.string.toast_failed_to_delete_user, Toast.LENGTH_SHORT).show();

                    });
        }
    }


    //This is to set limits on number of hours between 1 and 100
    //Code is taken and partyle edited from: https://www.techcompose.com/how-to-set-minimum-and-maximum-value-in-edittext-in-android-app-development/
    public static class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

}