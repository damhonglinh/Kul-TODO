package view.group;

import vn.edu.rmit.assignment1.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

public class GroupFormDialogFragment extends DialogFragment {
	private String title;
	private String id;
	private int position;

	static GroupFormDialogFragment newInstance(String id, String title,
			int position) {
		GroupFormDialogFragment df = new GroupFormDialogFragment();

		Bundle args = new Bundle();
		args.putString("id", id);
		args.putString("title", title);
		args.putInt("position", position);

		df.setArguments(args);
		return df;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());

		final KulOnGroupFormActionListener listener = (KulOnGroupFormActionListener) getActivity();

		final EditText edit = new EditText(getActivity());
		edit.setHint(getActivity().getString(R.string.enterGroupName));

		Bundle args = getArguments();
		id = args.getString("id");
		title = args.getString("title");
		position = args.getInt("position");

		ad.setView(edit);

		String positiveString;
		if (title != null) {
			ad.setTitle(getActivity().getString(R.string.changeGroupName)
					+ title + " to: ");
			positiveString = getActivity().getString(R.string.change);
			edit.setText(title);
		} else {
			ad.setTitle(getActivity().getString(R.string.addNewGroup));
			positiveString = getActivity().getString(R.string.add);
		}

		ad.setPositiveButton(positiveString,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String text = edit.getText().toString();
						if (text == null) {
							return;
						}
						listener.ok(id, text, position);
					}
				});

		ad.setNegativeButton(getActivity().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						listener.cancel();
					}
				});

		return ad.create();
	}

	interface KulOnGroupFormActionListener {
		void ok(String id, String name, int position);

		void cancel();
	}
}
