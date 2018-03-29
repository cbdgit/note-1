package com.example.exmword;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.aqi00.lib.dialog.FileSelectFragment;
import com.aqi00.lib.dialog.FileSelectFragment.FileSelectCallbacks;
import com.example.exmword.adapter.PdfSelfAdapter;
import com.example.exmword.util.FileUtil;
import com.example.exmword.util.MD5Util;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PdfSelfActivity extends FragmentActivity implements OnClickListener, FileSelectCallbacks {
	private final static String TAG = "PdfSelfActivity";
	private ViewPager vp_content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pdf_self);
		
		findViewById(R.id.btn_open).setOnClickListener(this);
		vp_content = (ViewPager) findViewById(R.id.vp_content);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_open) {
			FileSelectFragment.show(this, new String[] {"pdf"}, null);
		}
	}

	@Override
	public void onConfirmSelect(String absolutePath, String fileName, Map<String, Object> map_param) {
		String path = String.format("%s/%s", absolutePath, fileName);
		Log.d(TAG, "path="+path);
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				"/Download/pdf/" + MD5Util.encrypByMd5(path);
		ArrayList<String> imgArray = new ArrayList<String>();
		ProgressDialog pd = ProgressDialog.show(this, "请稍候", "正在努力加载"+fileName);
		try {
			ParcelFileDescriptor fd = ParcelFileDescriptor.open(
					new File(path), ParcelFileDescriptor.MODE_READ_ONLY);
			PdfRenderer pdfRenderer = new PdfRenderer(fd);
			Log.d(TAG, "page count="+pdfRenderer.getPageCount());
			for (int i=0; i<pdfRenderer.getPageCount(); i++) {
				String imgPath = String.format("%s/%d.jpg", dir, i);
				imgArray.add(imgPath);
				final PdfRenderer.Page page = pdfRenderer.openPage(i);
				Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),  
		                Bitmap.Config.ARGB_8888);
				page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
				FileUtil.saveBitmap(imgPath, bitmap);
				page.close();
			}
			pdfRenderer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pd!=null && pd.isShowing()) {
				pd.dismiss();
			}
		}

		PdfSelfAdapter adapter = new PdfSelfAdapter(getSupportFragmentManager(), imgArray);
		vp_content.setAdapter(adapter);
		vp_content.setCurrentItem(0);
		vp_content.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean isFileValid(String absolutePath, String fileName, Map<String, Object> map_param) {
		return true;
	}
	
}
