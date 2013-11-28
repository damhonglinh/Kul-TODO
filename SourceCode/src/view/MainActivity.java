package view;

import model.KulSync;
import view.group.GroupActivity;
import vn.edu.rmit.assignment1.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private final String AUTH_TOKEN_TYPE = "Manage your tasks";
	private final String CLIENT_ID = "49428392528.apps.googleusercontent.com";
	private final String CLIENT_SECRECT = "5Qat3CnfS83AkDAciWoWrlGj";
	private final String API_KEY = "AIzaSyDNyKHDxgmcss4PhrbXhxxm6-37PAoZ7uY";
	private final int CONTACT_CHOOSER = 99;
	private AccountManager accountManager;
	private AsyncTask<Void, Void, Void> kulSync;
	private Account[] accounts;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		Button skipBut = (Button) findViewById(R.id.mainActivitySkip);
		skipBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goToNextActivity();
				if (kulSync != null) {
					kulSync.cancel(true);
				}
			}
		});

		accountManager = AccountManager.get(this);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		String token = preferences.getString("token", "");

		if (token.equals("")) {
			showSyncOrNextActivityDialog("");
		} else {
			onReceiveAccountToken(token);
		}
	}

	private void authorizeAccount() {
		accountManager.invalidateAuthToken(AUTH_TOKEN_TYPE, null);
		accounts = accountManager.getAccountsByType("com.google");
		showDialog(CONTACT_CHOOSER);
	}

	private void obtainAccountToken(Account acc) {
		accountManager.getAuthToken(acc, AUTH_TOKEN_TYPE, null, this,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						try {
							String token = future.getResult().getString(
									AccountManager.KEY_AUTHTOKEN);
							Editor edit = preferences.edit();
							edit.putString("token", token);
							edit.commit();

							onReceiveAccountToken(token);
						} catch (OperationCanceledException e) {
							showSyncOrNextActivityDialog(getString(R.string.authorizeFailTryAgain));
						} catch (Exception e) {
							Log.i("TODO", e.toString() + " MainActivity 65");
							showSyncOrNextActivityDialog(getString(R.string.authorizeFailTryAgain));
						}
					}
				}, null);
	}

	private void onReceiveAccountToken(String token) {
		Log.i("TODO", "received Token and gonna pass to KulSync");
		kulSync = new KulSync(token, CLIENT_ID, CLIENT_SECRECT, API_KEY,
				MainActivity.this, GroupActivity.class).execute();
	}

	private void goToNextActivity() {
		Intent intent = new Intent(this, GroupActivity.class);
		intent.putExtra("text", getString(R.string.syncSkip));
		startActivity(intent);
		finish();
	}

	private void showSyncOrNextActivityDialog(String text) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ConfirmDialogFragment df = ConfirmDialogFragment.newInstance(
				new ConfirmDialogFragment() {
					@Override
					public void confirm() {
						authorizeAccount();
					}

					@Override
					public void cancel() {
						goToNextActivity();
					}
				}, getString(R.string.syncWithGoogleQues),
				getString(R.string.askPermissionToSync));
		df.show(ft, "confirm sync");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTACT_CHOOSER) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.selectGoogleAccount));
			final int size = accounts.length;
			String[] names = new String[size];
			for (int i = 0; i < size; i++) {
				names[i] = accounts[i].name;
			}
			builder.setItems(names, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int position) {
					obtainAccountToken(accounts[position]);
				}
			});
			return builder.create();
		} else {
			return null;
		}
	}

	private void exit() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
