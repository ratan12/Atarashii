package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class Card extends RelativeLayout {
    public boolean center;
    public TextView Header;
    public RelativeLayout Card;
    public RelativeLayout Content;
    
    int screenWidth;
    int minHeight;
    Float density;

    LayoutInflater inflater;
    onCardClickListener listener;

    public Card(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Get attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Card, 0, 0);
        center = a.getBoolean(R.styleable.Card_header_Title_center, false);
        String TitleText = a.getString(R.styleable.Card_header_Title);
        int TitleColor = a.getResourceId(R.styleable.Card_header_Title_Color, android.R.color.black);
        int HeaderColor = a.getResourceId(R.styleable.Card_header_Color, R.color.card_content);
        Integer maxWidth = a.getInteger(R.styleable.Card_card_maxWidth, 0);
        minHeight = a.getInteger(R.styleable.Card_card_minHeight, 0);
        Integer divide = a.getInteger(R.styleable.Card_card_divide, 0);

        // Setup layout
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.card_layout_base, this);

        // Get the views
        Card = (RelativeLayout) findViewById(R.id.BaseCard);
        Content = (RelativeLayout) findViewById(R.id.content);
        Header = (TextView) findViewById(R.id.CardTitle);

        // Apply attributes
        if (divide != 0 || maxWidth != 0)
            setWidth(divide, maxWidth);
        Header.setText(TitleText);
        Header.setTextColor(getResources().getColor(TitleColor));
        setHeaderColor(HeaderColor);

        a.recycle();
    }

    /**
     * Add content to the card.
     *
     * @param resource The resource id that you want to appear in the card
     */
    public void setContent(int resource) {
        inflater.inflate(resource, Content);
        if (this.findViewById(R.id.ListView) != null)
            Content.setPadding(0, 0, 0, Content.getPaddingBottom() / 12 * 6);
    }

    /**
     * Change the content padding of a card.
     *
     * @param left   The padding of the left side in dp
     * @param top    The padding of the top in dp
     * @param right  The padding of the right side in dp
     * @param bottom The padding of the bottom in dp
     */
    public void setPadding(int left, int top, int right, int bottom) {
        Content.setPadding(convert(left), convert(top), convert(right), (bottom != -1 ? bottom : convert(4)));
    }

    /**
     * Change the content padding of a card.
     *
     * @param all The padding of all the sides
     */
    public void setPadding(int all) {
        all = convert(all);
        Content.setPadding(all, all, all, all);
    }

    /**
     * Recalculate the required height of a listview and apply it.
     *
     * @param total        The total number of items
     * @param normalHeight The default height of headers in dp
     * @param headers      The number of headers
     * @param headerHeight The height of a header in dp
     * @param divider      The thickness of the divider in dp
     */
    public void refreshList(int total, int normalHeight, int headers, int headerHeight, int divider) {
        if (total == 0) {
            this.setVisibility(View.GONE);
        } else {
            Integer normal = total - headers;

            int Height = normal * normalHeight;
            Height = Height + (headers * headerHeight);
            Height = Height + ((total - 1) * divider);

            if (this.findViewById(R.id.ListView) != null)
                this.findViewById(R.id.ListView).getLayoutParams().height = convert(Height);
        }
    }

    /**
     * Set the card at the right side of another card.
     *
     * @param res    The card at the left side of your desired point
     * @param amount The amount of cards that will be at the left & right sides of your desired point
     *               Note: This also includes this card it self
     * @param screen The minimum amount of dp when the card will be placed at the right side
     *               Note: Use 0 if you don't want any
     */
    public void setRightof(Card res, int amount, int screen) {
        if (convert(screen) <= getScreenWidth()) {
            RelativeLayout.LayoutParams card = new LayoutParams(getWidth(amount, 0), convert(minHeight));
            card.addRule(RelativeLayout.RIGHT_OF, res.getId());
            card.setMargins(convert(4), 0, 0, 0);
            this.setLayoutParams(card);
        }
    }

    /**
     * Change the header color.
     *
     * @param color The resource id of the color
     */
    public void setHeaderColor(int color) {
        GradientDrawable shape = (GradientDrawable) Header.getBackground();
        shape.setColor(getResources().getColor(color));
    }

    /**
     * Create an onClick event for listening for clicks.
     *
     * @param view     The view id that will trigger the interface method
     * @param callback The activity that contains the interface method
     */
    public void setOnClickListener(int view, onCardClickListener callback) {
        listener = callback;
        (this.findViewById(R.id.actionableIcon)).setVisibility(View.VISIBLE);
        Content.findViewById(view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCardClickListener(v.getId());
            }
        });
    }

    /**
     * Wraps the width of a card
     *
     * @param header This won't cut off the text in the header if it is true
     */
    public void wrapWidth(boolean header) {
        int width;
        Header.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Card.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        if (Content.getMeasuredWidth() >= Header.getMeasuredWidth() || !header)
            width = Content.getMeasuredWidth();
        else
            width = Header.getMeasuredWidth();

        Header.setLayoutParams(new LayoutParams(width, Header.getLayoutParams().height));
        Card.setLayoutParams(new LayoutParams(width, Card.getMeasuredHeight()));
        if (center)
            Header.setGravity(Gravity.CENTER);
    }

    /**
     * Set the card width.
     *
     * @param amount   The amount of cards besides each other
     * @param maxWidth The maximum width in dp
     */
    public void setWidth(Integer amount, Integer maxWidth) {
        Card.getLayoutParams().width = getWidth(amount, maxWidth);
    }

    /**
     * Get the card width.
     *
     * @param amount   The amount of cards besides each other
     * @param maxWidth The maximum width in dp
     * @return int The width that the card should be
     */
    public int getWidth(Integer amount, Integer maxWidth) {
        if (amount == 0)
            amount = 1;
        int divider = amount - 1;
        divider = convert((divider * 4) + 16);
        int card = (getScreenWidth() - divider) / amount;
        maxWidth = convert(maxWidth);

        if (card > maxWidth && maxWidth != 0)
            return maxWidth;
        else
            return card;
    }

    /**
     * The Interface that will get triggered by the OnClick method.
     */
    public interface onCardClickListener {
        public void onCardClickListener(int id);
    }

    /**
     * Get the display density.
     *
     * @return Float The display density
     */
    private Float getDensity() {
        if (density == null)
            density = (getResources().getDisplayMetrics().densityDpi / 160f);
        return density;
    }

    /**
     * Convert dp to pixels.
     *
     * @param number The number in dp to convert in pixels
     * @return int The converted dp in pixels
     */
    private int convert(int number) {
        return (int) (getDensity() * number);
    }

    /**
     * Get the screen width.
     *
     * @return int The screen width in pixels
     */
    @SuppressLint("InlinedApi")
    private int getScreenWidth() {
        if (screenWidth == 0) {
            try {
                screenWidth = convert(getResources().getConfiguration().screenWidthDp);
            } catch (NoSuchFieldError e) {
                screenWidth = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
            }
        }
        return screenWidth;
    }
}