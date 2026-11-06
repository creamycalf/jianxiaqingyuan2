package com.upwinded.jxqy;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends Activity
{
	private static final int MIN_TEXT_SIZE_SP = 14;
	private static final int MAX_TEXT_SIZE_SP = 30;
	private static final int DEFAULT_TEXT_SIZE_SP = 18;

	private final List<String> chapterTitles = new ArrayList<>();
	private final List<Integer> chapterOffsets = new ArrayList<>();
	private String guideText;
	private TextView contentView;
	private ScrollView scrollView;
	private Spinner chapterSpinner;
	private EditText searchInput;
	private TextView fontSizeView;
	private int fontSizeSp = DEFAULT_TEXT_SIZE_SP;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setStatusBarColor(Color.rgb(28, 38, 32));
		getWindow().setNavigationBarColor(Color.rgb(28, 38, 32));
		loadGuide();
		buildInterface();
	}

	private void loadGuide()
	{
		try (InputStream input = getAssets().open("engine/guide/guide.txt");
			 ByteArrayOutputStream output = new ByteArrayOutputStream())
		{
			byte[] buffer = new byte[8192];
			int count;
			while ((count = input.read(buffer)) != -1)
			{
				output.write(buffer, 0, count);
			}
			guideText = new String(output.toByteArray(), StandardCharsets.UTF_8);
			findChapters();
		}
		catch (Exception exception)
		{
			guideText = null;
		}
	}

	private void findChapters()
	{
		chapterTitles.clear();
		chapterOffsets.clear();
		String[] lines = guideText.split("\\n", -1);
		int offset = 0;
		for (String line : lines)
		{
			String trimmed = line.trim();
			if (trimmed.matches("#{1,3}\\s+.+") ||
				trimmed.matches("第.{1,24}[章节篇回].*") ||
				(trimmed.length() > 1 && trimmed.endsWith("、") && trimmed.length() < 40))
			{
				String title = trimmed.replaceFirst("^#{1,3}\\s+", "");
				chapterTitles.add(title);
				chapterOffsets.add(offset);
			}
			offset += line.length() + 1;
		}
		if (chapterTitles.isEmpty())
		{
			chapterTitles.add("全文");
			chapterOffsets.add(0);
		}
	}

	private void buildInterface()
	{
		int padding = dp(12);
		LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setPadding(padding, padding, padding, padding);
		root.setBackgroundColor(Color.rgb(244, 246, 242));
		root.setFocusableInTouchMode(true);
		root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener()
		{
			@Override
			public WindowInsets onApplyWindowInsets(View view, WindowInsets insets)
			{
				int left;
				int top;
				int right;
				int bottom;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
				{
					android.graphics.Insets safeInsets = insets.getInsets(
						WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
					left = safeInsets.left;
					top = safeInsets.top;
					right = safeInsets.right;
					bottom = safeInsets.bottom;
				}
				else
				{
					left = insets.getSystemWindowInsetLeft();
					top = insets.getSystemWindowInsetTop();
					right = insets.getSystemWindowInsetRight();
					bottom = insets.getSystemWindowInsetBottom();
				}
				view.setPadding(padding + left, padding + top,
					padding + right, padding + bottom);
				return insets;
			}
		});
		setContentView(root);
		root.requestApplyInsets();

		LinearLayout heading = new LinearLayout(this);
		heading.setGravity(Gravity.CENTER_VERTICAL);
		root.addView(heading, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
		TextView title = new TextView(this);
		title.setText("离线攻略");
		title.setTextSize(20);
		title.setTextColor(Color.rgb(35, 48, 39));
		title.setTypeface(null, android.graphics.Typeface.BOLD);
		title.setAccessibilityHeading(true);
		heading.addView(title, new LinearLayout.LayoutParams(0,
			LinearLayout.LayoutParams.WRAP_CONTENT, 1));
		Button back = makeButton("返回游戏");
		back.setContentDescription("关闭攻略并返回游戏");
		back.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				finish();
			}
		});
		heading.addView(back, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT, dp(44)));

		if (guideText == null)
		{
			TextView missing = new TextView(this);
			missing.setText("未找到离线攻略文件。\n\n请将 UTF-8 攻略文件放置于应用 assets/engine/guide/guide.txt。当前没有可显示的攻略内容。");
			missing.setTextSize(DEFAULT_TEXT_SIZE_SP);
			missing.setTextColor(Color.DKGRAY);
			missing.setGravity(Gravity.CENTER);
			missing.setPadding(dp(24), dp(24), dp(24), dp(24));
			root.addView(missing, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
			return;
		}

		LinearLayout chapterRow = new LinearLayout(this);
		chapterRow.setGravity(Gravity.CENTER_VERTICAL);
		root.addView(chapterRow, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
		TextView chapterLabel = new TextView(this);
		chapterLabel.setText("章节");
		chapterLabel.setTextColor(Color.DKGRAY);
		chapterRow.addView(chapterLabel, new LinearLayout.LayoutParams(
			dp(44), LinearLayout.LayoutParams.WRAP_CONTENT));
		chapterSpinner = new Spinner(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
			android.R.layout.simple_spinner_item, chapterTitles);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		chapterSpinner.setAdapter(adapter);
		chapterRow.addView(chapterSpinner, new LinearLayout.LayoutParams(0,
			LinearLayout.LayoutParams.MATCH_PARENT, 1));
		chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if (contentView != null)
				{
					int chapterOffset = chapterOffsets.get(position);
					contentView.post(new Runnable()
					{
						@Override
						public void run()
						{
							scrollView.smoothScrollTo(0, lineTop(chapterOffset));
						}
					});
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		LinearLayout searchRow = new LinearLayout(this);
		searchRow.setGravity(Gravity.CENTER_VERTICAL);
		root.addView(searchRow, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
		searchInput = new EditText(this);
		searchInput.setSingleLine(true);
		searchInput.setHint("搜索攻略");
		searchInput.setTextSize(16);
		searchInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
		searchRow.addView(searchInput, new LinearLayout.LayoutParams(0,
			LinearLayout.LayoutParams.MATCH_PARENT, 1));
		Button find = makeButton("查找");
		find.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				searchForText();
			}
		});
		searchRow.addView(find, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT, dp(44)));
		searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView text, int actionId,
				android.view.KeyEvent event)
			{
				searchForText();
				return true;
			}
		});

		LinearLayout sizeRow = chapterRow;
		Button smaller = makeButton("A−");
		smaller.setContentDescription("减小字号");
		smaller.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View view) { setFontSize(fontSizeSp - 2); }
		});
		sizeRow.addView(smaller, new LinearLayout.LayoutParams(dp(48), dp(40)));
		fontSizeView = new TextView(this);
		fontSizeView.setGravity(Gravity.CENTER);
		fontSizeView.setTextColor(Color.DKGRAY);
		sizeRow.addView(fontSizeView, new LinearLayout.LayoutParams(dp(56),
			LinearLayout.LayoutParams.MATCH_PARENT));
		Button larger = makeButton("A+");
		larger.setContentDescription("增大字号");
		larger.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View view) { setFontSize(fontSizeSp + 2); }
		});
		sizeRow.addView(larger, new LinearLayout.LayoutParams(dp(48), dp(40)));

		scrollView = new ScrollView(this);
		scrollView.setFillViewport(true);
		scrollView.setFocusable(true);
		root.addView(scrollView, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
		contentView = new TextView(this);
		contentView.setText(guideText);
		contentView.setTextColor(Color.rgb(38, 42, 38));
		contentView.setTextIsSelectable(true);
		contentView.setLineSpacing(dp(3), 1.0f);
		contentView.setPadding(dp(4), dp(12), dp(4), dp(24));
		scrollView.addView(contentView, new ScrollView.LayoutParams(
			ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
		setFontSize(DEFAULT_TEXT_SIZE_SP);
	}

	private Button makeButton(String label)
	{
		Button button = new Button(this);
		button.setText(label);
		button.setTextSize(14);
		button.setAllCaps(false);
		button.setMinHeight(dp(40));
		button.setMinimumHeight(dp(40));
		return button;
	}

	private void setFontSize(int size)
	{
		fontSizeSp = Math.max(MIN_TEXT_SIZE_SP, Math.min(MAX_TEXT_SIZE_SP, size));
		if (contentView != null)
		{
			contentView.setTextSize(fontSizeSp);
		}
		if (fontSizeView != null)
		{
			fontSizeView.setText(fontSizeSp + " sp");
		}
	}

	private int lineTop(int offset)
	{
		Layout layout = contentView.getLayout();
		if (layout == null || layout.getLineCount() == 0)
		{
			return 0;
		}
		int line = layout.getLineForOffset(Math.min(offset, guideText.length()));
		return layout.getLineTop(line);
	}

	private void searchForText()
	{
		String query = searchInput.getText().toString().trim();
		if (query.isEmpty())
		{
			return;
		}
		InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (keyboard != null) keyboard.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
		int start = 0;
		Layout currentLayout = contentView.getLayout();
		if (currentLayout != null)
		{
			int visibleLine = currentLayout.getLineForVertical(scrollView.getScrollY());
			start = currentLayout.getOffsetForHorizontal(visibleLine, 0);
		}
		int match = guideText.toLowerCase(java.util.Locale.ROOT).indexOf(
			query.toLowerCase(java.util.Locale.ROOT), start);
		if (match < 0 && start > 0)
		{
			match = guideText.toLowerCase(java.util.Locale.ROOT).indexOf(
				query.toLowerCase(java.util.Locale.ROOT));
		}
		if (match < 0)
		{
			android.widget.Toast.makeText(this, "未找到相关内容", android.widget.Toast.LENGTH_SHORT).show();
			return;
		}
		final int target = match;
		contentView.post(new Runnable()
		{
			@Override
			public void run()
			{
				Layout layout = contentView.getLayout();
				if (layout != null)
				{
					scrollView.smoothScrollTo(0, layout.getLineTop(
						layout.getLineForOffset(target)));
				}
			}
		});
	}

	private int dp(int value)
	{
		return Math.round(value * getResources().getDisplayMetrics().density);
	}
}
