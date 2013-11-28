package view;

import vn.edu.rmit.assignment1.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public abstract class ConfirmDialogFragment extends DialogFragment {
	private String title;
	private String content;

	public static ConfirmDialogFragment newInstance(ConfirmDialogFragment df,
			String title, String content) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("content", content);

		df.setArguments(args);
		return df;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(
				getActivity());

		Bundle args = getArguments();
		content = args.getString("content");
		title = args.getString("title");
		dialog.setMessage(content);
		dialog.setTitle(title);

		dialog.setPositiveButton(getActivity().getString(R.string.yeah),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						confirm();
					}
				});

		dialog.setNegativeButton(getActivity().getString(R.string.nope),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancel();
					}
				});

		return dialog.create();
	}

	public abstract void confirm();

	public void cancel() {
		dismiss();
	}
}
