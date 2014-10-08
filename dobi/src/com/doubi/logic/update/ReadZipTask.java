package com.doubi.logic.update;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.dobi.R;

public class ReadZipTask extends AsyncTask<Void, Void, Void> {

	private static ProgressDialog pd;
	private Activity context;

	public ReadZipTask(Activity context) {

		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pd = ProgressDialog.show(context, R.string.wait+"", R.string.decompression+"");
	}

	@Override
	protected Void doInBackground(Void... params) {
		return null;
	}
	protected void closeDialog(){
		pd.dismiss();
	}

}
