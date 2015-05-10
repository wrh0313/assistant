package com.wrh.assistant.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wrh.assistant.R;

public class NearbyQCheckItem extends LinearLayout {
	private ImageView divider;
	private LinearLayout main;
	private int mainHeight;
	private TextView tvType;
	private TextView tvChild1;
	private TextView tvChild2;
	private TextView tvChild3;
	private OnClickListener mOnClickListener;
	private OnClickItemListener mClickItemListener;

	public interface OnClickItemListener {
		void onItemClick(View v);
	}

	public NearbyQCheckItem(Context context) {
		super(context);

		init();
	}

	public void setOnClickItemListener(OnClickItemListener l) {
		mClickItemListener = l;
	}

	public void setItemText(String type, String child1, String child2,
			String child3) {
		tvType.setText(type);
		tvChild1.setText(child1);
		tvChild2.setText(child2);
		tvChild3.setText(child3);
		mOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mClickItemListener != null) {
					mClickItemListener.onItemClick(v);
				}
			}
		};
		tvChild1.setOnClickListener(mOnClickListener);
		tvChild2.setOnClickListener(mOnClickListener);
		tvChild3.setOnClickListener(mOnClickListener);
	}

	private void init() {
		setOrientation(LinearLayout.VERTICAL);

		divider = new ImageView(getContext());
		LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		divider.setLayoutParams(dividerParams);
		divider.setImageResource(R.drawable.list_line);
		addView(divider);

		main = (LinearLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.item_nearby_quick_check, null);

		main.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						TextView tv = (TextView) main.getChildAt(0);
						mainHeight = tv.getMeasuredHeight();
						main.getLayoutParams().height = mainHeight;
					}
				});
		tvType = (TextView) main.findViewById(R.id.type);
		tvChild1 = (TextView) main.findViewById(R.id.child1);
		tvChild2 = (TextView) main.findViewById(R.id.child2);
		tvChild3 = (TextView) main.findViewById(R.id.child3);
		addView(main);
	}
}
