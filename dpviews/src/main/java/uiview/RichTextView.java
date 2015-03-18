package uiview;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TextView with support for custom markup for formatting the text
 *
 * Supported markups are
 *
 * [FONT:<font-name>]Ipsum ...[/FONT]
 * [COLOR:<hex-color>]Ipsum ...[/FONT]
 * [RELATIVE_SIZE:<relative-size-float>]Ipsum ...[/RELATIVE_SIZE]
 * [UNDERLINE]Ipsum ...[/UNDERLINE]
 * [URL:<url-address>]Ipsum ...[/URL]
 *
 * Created by david on 07/07/14.
 */
public class RichTextView extends TextView {

    /**
     * Logger tag
     */
    private static final String TAG = RichTextView.class.getName();

    /**
     * Regexp properties
     */
    private static final int ENTIRE_REGEXP_GROUP = 0;
    private static final int START_TAG_REGEX_GROUP = 1;
    private static final int PROPERTY_REGEX_GROUP = 2;
    private static final int VALUE_REGEX_GROUP = 3;
    private static final int END_TAG_REGEX_GROUP = 4;

    /**
     * Defined expressions to match markup
     */
    private static final String REGEXP_BASE = "(\\[%s:(.*?)\\])(.*)(\\[/%s\\])";
    private static final String FONT_REGEX = String.format(REGEXP_BASE, "FONT", "FONT");
    private static final String COLOR_REGEX = String.format(REGEXP_BASE, "COLOR", "COLOR");
    private static final String SIZE_REGEX = String.format(REGEXP_BASE, "RELATIVE_SIZE", "RELATIVE_SIZE");
    private static final String UNDERLINE_REGEX = String.format(REGEXP_BASE, "UNDERLINE", "UNDERLINE");
    private static final String URL_REGEX = String.format(REGEXP_BASE, "URL", "URL");

    /**
     * Defined regexp markup patterns
     */
    private static final Pattern FONT_PATTERN = Pattern.compile(FONT_REGEX);
    private static final Pattern COLOR_PATTERN = Pattern.compile(COLOR_REGEX);
    private static final Pattern SIZE_PATTERN = Pattern.compile(SIZE_REGEX);
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile(UNDERLINE_REGEX);
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    private interface MatchListener {
        CharacterStyle onMatch(String property);
    }

    /**
     *
     * @param context
     */
    public RichTextView(Context context) {
        super(context);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public RichTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setProperties();
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public RichTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setProperties();
    }

    /**
     * Sets the text that this TextView is to display (see
     * {@link #setText(CharSequence)}) and also sets whether it is stored
     * in a styleable/spannable buffer and whether it is editable.
     *
     * @attr ref android.R.styleable#TextView_text
     * @attr ref android.R.styleable#TextView_bufferType
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        setProperties();
    }

    /**
     * Set properties from the markup.
     * Call this method to set properties from the markup
     */
    public void setProperties() {
        CharSequence text = getText();
        if (text == null || text.length() == 0) {
            return;
        }
        Log.i(TAG, "Render:" + text.toString());
        setProperty(FONT_PATTERN, new MatchListener() {
            @Override
            public CharacterStyle onMatch(String property) {
                return new TypefaceSpan(property);
            }
        });

        setProperty(COLOR_PATTERN, new MatchListener() {
            @Override
            public CharacterStyle onMatch(String property) {
                int color = Integer.parseInt(property);
                return new ForegroundColorSpan(color);
            }
        });

        setProperty(SIZE_PATTERN, new MatchListener() {
            @Override
            public CharacterStyle onMatch(String property) {
                float size = Float.valueOf(property);
                return new RelativeSizeSpan(size);
            }
        });

        setProperty(UNDERLINE_PATTERN, new MatchListener() {
            @Override
            public CharacterStyle onMatch(String property) {
                return new UnderlineSpan();
            }
        });

        setProperty(URL_PATTERN, new MatchListener() {
            @Override
            public CharacterStyle onMatch(String property) {
                return new URLSpan(property);
            }
        });
    }


    /**
     * Find the property using the provided pattern and
     * call the listener to get the corresponding character style
     *
     * @param pattern       matching pattern
     * @param matchListener
     */
    private void setProperty(Pattern pattern, MatchListener matchListener) {
        try {
            final CharSequence spannableText = getText();
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(spannableText);
            Matcher matcher = pattern.matcher(spannableStringBuilder);
            while (matcher.find()) {
                int startBeginDelimiter = matcher.start(START_TAG_REGEX_GROUP);
                int endBeginDelimiter = matcher.end(START_TAG_REGEX_GROUP);
                int startProperty = matcher.start(PROPERTY_REGEX_GROUP);
                int endProperty = matcher.end(PROPERTY_REGEX_GROUP);
                int startValue = matcher.start(VALUE_REGEX_GROUP);
                int endValue = matcher.end(VALUE_REGEX_GROUP);
                int startEndDelimiter = matcher.start(END_TAG_REGEX_GROUP);
                int endEndDelimiter = matcher.end(END_TAG_REGEX_GROUP);
                String property = spannableStringBuilder.subSequence(startProperty, endProperty).toString();
                Object spanObject = matchListener.onMatch(property);
                spannableStringBuilder.setSpan(spanObject, startValue, endValue, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.delete(startEndDelimiter, endEndDelimiter);
                spannableStringBuilder.delete(startBeginDelimiter, endBeginDelimiter);
            }
            super.setText(spannableStringBuilder, BufferType.SPANNABLE);
        } catch (RuntimeException rte) {
            Log.e(TAG, "Unable to format spannable", rte);
        }
    }

}
