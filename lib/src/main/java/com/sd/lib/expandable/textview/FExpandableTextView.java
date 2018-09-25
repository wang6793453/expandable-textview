package com.sd.lib.expandable.textview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FExpandableTextView extends AppCompatTextView
{
    public FExpandableTextView(Context context)
    {
        super(context);
        init();
    }

    public FExpandableTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public FExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private SpannableStringBuilder mBuilder;

    private CharSequence mOriginalText = "";
    private BufferType mOriginalBufferType;

    private State mState = State.Shrink;
    private int mLimitLineCount = 2;

    private String mTextShowMore = "展开";
    private String mTextHideMore = "收起";

    private int mTextColorShowMore = Color.BLUE;
    private int mTextColorHideMore = Color.BLUE;

    private boolean mProcess;

    private TextView mTextView;

    private void init()
    {
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.TRANSPARENT);
    }

    /**
     * 设置限制行数
     *
     * @param limitLineCount
     */
    public void setLimitLineCount(int limitLineCount)
    {
        if (limitLineCount > 0 && limitLineCount != mLimitLineCount)
        {
            mLimitLineCount = limitLineCount;
            invalidate();
        }
    }

    /**
     * 设置“更多”字符串
     *
     * @param textShowMore
     */
    public void setTextShowMore(String textShowMore)
    {
        mTextShowMore = textShowMore;
    }

    /**
     * 设置“收起”字符串
     *
     * @param textHideMore
     */
    public void setTextHideMore(String textHideMore)
    {
        mTextHideMore = textHideMore;
    }

    /**
     * 设置“更多”字符串颜色
     *
     * @param textColorShowMore
     */
    public void setTextColorShowMore(int textColorShowMore)
    {
        mTextColorShowMore = textColorShowMore;
    }

    /**
     * 设置“收起”字符串颜色
     *
     * @param textColorHideMore
     */
    public void setTextColorHideMore(int textColorHideMore)
    {
        mTextColorHideMore = textColorHideMore;
    }

    private void setTextInternal(CharSequence text)
    {
        if (text != null && text.toString().equals(getText().toString()))
        {
            Log.e(FExpandableTextView.class.getSimpleName(), "setTextInternal:" + text);
            return;
        }

        super.setText(text, mOriginalBufferType);
        Log.i(FExpandableTextView.class.getSimpleName(), "setTextInternal:" + text);
    }

    @Override
    public void setText(CharSequence text, BufferType type)
    {
        if (text == null)
            text = "";

        mOriginalText = text;
        mOriginalBufferType = type;
        super.setText(text, type);
    }

    private TextView getTextView()
    {
        if (mTextView == null)
            mTextView = new TextView(getContext());
        return mTextView;
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params)
    {
        getTextView().setLayoutParams(params);
        super.setLayoutParams(params);
    }

    @Override
    public void setTextColor(int color)
    {
        getTextView().setTextColor(color);
        super.setTextColor(color);
    }

    @Override
    public void setTextColor(ColorStateList colors)
    {
        getTextView().setTextColor(colors);
        super.setTextColor(colors);
    }

    @Override
    public void setTextSize(int unit, float size)
    {
        getTextView().setTextSize(unit, size);
        super.setTextSize(unit, size);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        getTextView().measure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mProcess = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        getTextView().layout(left, top, right, bottom);
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        processIfNeed();
    }

    private SpannableStringBuilder getBuilder()
    {
        if (mBuilder == null)
            mBuilder = new SpannableStringBuilder();
        return mBuilder;
    }

    private void processIfNeed()
    {
        if (!mProcess)
        {
            setTextInternal(mOriginalText);
            return;
        }

        getTextView().setText(mOriginalText);
        final Layout layout = getTextView().getLayout();
        if (layout == null)
            return;

        final int originalLineCount = layout.getLineCount();
        if (originalLineCount <= mLimitLineCount)
        {
            setTextInternal(mOriginalText);
            return;
        }

        if (mState == State.Shrink)
        {
            processShrink(originalLineCount);
        } else
        {
            processExpand(originalLineCount);
        }
    }

    /**
     * 收起状态下的逻辑
     */
    private void processShrink(int originalLineCount)
    {
        final String textSuffix = mTextShowMore;
        CharSequence textContent = null;

        final int stepLength = (int) getPaint().measureText(" ");
        final int limitLength = (getLayout().getWidth() * mLimitLineCount) - getPaddingLeft() - getPaddingRight();
        final int suffixLength = (int) (getPaint().measureText(textSuffix) + 0.5f);

        int length = limitLength - suffixLength;
        int loopCount = 0;
        while (true)
        {
            if (length <= 0)
                break;

            final CharSequence ellipsize = TextUtils.ellipsize(mOriginalText, getPaint(), length, TextUtils.TruncateAt.END);
            getTextView().setText(ellipsize + textSuffix);
            final int lineCount = getTextView().getLineCount();

            if (lineCount > mLimitLineCount)
            {
                length -= stepLength;
                loopCount++;
                continue;
            } else
            {
                textContent = ellipsize;
                break;
            }
        }

        if (loopCount > 0)
            Log.i(FExpandableTextView.class.getSimpleName(), "loop ellipsize:" + loopCount);

        if (TextUtils.isEmpty(textContent))
            textContent = mOriginalText;

        getBuilder().clear();
        getBuilder().append(textContent);
        getBuilder().append(textSuffix);

        final int end = getBuilder().length();
        final int start = end - textSuffix.length();
        getBuilder().setSpan(mClickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        setTextInternal(getBuilder());
    }

    /**
     * 展开状态下的逻辑
     */
    private void processExpand(int originalLineCount)
    {
        final String textSuffix = mTextHideMore;
        if (TextUtils.isEmpty(textSuffix))
        {
            setTextInternal(mOriginalText);
            return;
        }

        CharSequence textContent = mOriginalText;

        int loopCount = 0;
        while (true)
        {
            final String text = textContent + textSuffix;

            getTextView().setText(text);
            final int lineCount = getTextView().getLineCount();
            final Layout layout = getTextView().getLayout();

            if (lineCount > originalLineCount)
            {
                final int start = layout.getLineEnd(lineCount - 2);
                final String textEnd = text.substring(start).trim();
                if (textSuffix.equals(textEnd))
                {
                    break;
                } else
                {
                    textContent = textContent + " ";
                    loopCount++;
                    continue;
                }
            } else
            {
                break;
            }
        }

        if (loopCount > 0)
            Log.i(FExpandableTextView.class.getSimpleName(), "loop space:" + loopCount);

        getBuilder().clear();
        getBuilder().append(textContent);
        getBuilder().append(textSuffix);

        final int end = getBuilder().length();
        final int start = end - textSuffix.length();
        getBuilder().setSpan(mClickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        setTextInternal(getBuilder());
    }

    private void setState(State state)
    {
        if (mState != state)
        {
            mState = state;
            invalidate();
        }
    }

    private final ClickableSpan mClickableSpan = new ClickableSpan()
    {
        @Override
        public void updateDrawState(TextPaint ds)
        {
            ds.setColor(Color.BLUE);
        }

        @Override
        public void onClick(View widget)
        {
            if (mState == State.Expand)
                setState(State.Shrink);
            else
                setState(State.Expand);
        }
    };

    enum State
    {
        Expand,
        Shrink,
    }
}