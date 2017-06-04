package net.somethingdreadful.MAL

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import net.somethingdreadful.MAL.adapters.DetailViewRelationsAdapter

class Card @JvmOverloads constructor(private val appContext: Context, attrs: AttributeSet, defStyle: Int = 0) : RelativeLayout(appContext, attrs, defStyle) {
    private val center: Boolean
    val Header: TextView
    private var Image: ImageView? = null
    private val Card: CardView
    private val Content: RelativeLayout?

    private var listener: onCardClickListener? = null
    private var screenWidth: Int = 0
    private val inflater: LayoutInflater

    init {
        // Get attributes
        val a = appContext.obtainStyledAttributes(attrs, R.styleable.Card, 0, 0)
        center = a.getBoolean(R.styleable.Card_header_Title_center, false)
        val TitleText = a.getString(R.styleable.Card_header_Title)
        val TitleColor = a.getResourceId(R.styleable.Card_header_Title_Color, android.R.color.black)
        val HeaderColor = a.getResourceId(R.styleable.Card_header_Color, R.color.bg_light)
        val maxWidth = a.getInteger(R.styleable.Card_card_maxWidth, 0)
        val divide = a.getInteger(R.styleable.Card_card_divide, 0)
        val content = a.getResourceId(R.styleable.Card_card_content, 0)

        // Setup layout
        inflater = appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.card_layout_base, this)

        // Get the views
        Card = findViewById(R.id.BaseCard) as CardView
        Content = findViewById(R.id.content) as RelativeLayout
        Header = findViewById(R.id.CardTitle) as TextView

        // Apply attributes
        if (divide !== 0 || maxWidth !== 0)
            setWidth(divide, maxWidth)
        Header.text = TitleText
        Header.setTextColor(ContextCompat.getColor(appContext, TitleColor))
        setHeaderColor(HeaderColor)
        if (content != 0)
            setContent(content)

        a.recycle()
    }

    /**
     * Add content to the card.

     * @param resource The resource id that you want to appear in the card
     */
    fun setContent(resource: Int) {
        inflater.inflate(resource, Content)
        if (this.findViewById(R.id.ListView) != null)
            setPadding(0)
        if (Theme.darkTheme) {
            Content!!.setBackgroundColor(ContextCompat.getColor(appContext, R.color.cardview_dark_background))
            initLoop(Content)
            if (this.findViewById(R.id.ListView) != null) {
                val listView = this.findViewById(R.id.ListView) as ExpandableListView
                val colorDrawable = ColorDrawable(ContextCompat.getColor(appContext, R.color.bg_dark_card))
                listView.divider = colorDrawable
                listView.setChildDivider(colorDrawable)
                listView.dividerHeight = Theme.convert(1)
            }
        }
    }

    private fun initLoop(view: TableRow) {
        for (i in 0..view.childCount - 1) {
            val child = view.getChildAt(i)
            if (child is TextView)
                child.setTextColor(ContextCompat.getColor(appContext, R.color.text_dark))
            else if (child is RelativeLayout) {
                initLoop(child)
                Theme.setBackground(context, child)
            } else if (child.id > 0 && resources.getResourceEntryName(child.id).contains("divider"))
                child.setBackgroundColor(ContextCompat.getColor(appContext, R.color.bg_dark_card))
        }
    }

    /**
     * Loop to apply themes on cards

     * @param view The view that should be themed.
     */
    private fun initLoop(view: RelativeLayout) {
        for (i in 0..view.childCount - 1) {
            val child = view.getChildAt(i)
            if (child is TextView)
                child.setTextColor(ContextCompat.getColor(appContext, R.color.text_dark))
            else if (child is RelativeLayout) {
                initLoop(child)
                Theme.setBackground(context, child)
            } else if (child is TableRow) {
                initLoop(child)
            } else if (child.id > 0 && resources.getResourceEntryName(child.id).contains("divider"))
                child.setBackgroundColor(ContextCompat.getColor(appContext, R.color.bg_dark_card))
        }
    }

    /**
     * Change the content padding of a card.

     * @param left   The padding of the left side in dp
     * *
     * @param top    The padding of the top in dp
     * *
     * @param right  The padding of the right side in dp
     * *
     * @param bottom The padding of the bottom in dp
     */
    fun setAllPadding(left: Int, top: Int, right: Int, bottom: Int) {
        Content?.setPadding(Theme.convert(left), Theme.convert(top), Theme.convert(right), Theme.convert(bottom))
    }

    /**
     * Change the content padding of a card.

     * @param all The padding of all the sides
     */
    private fun setPadding(all: Int) {
        setAllPadding(all, all, all, all)
    }

    /**
     * Recalculate the required height of a listview and apply it.

     * @param adapter The listadapter
     */
    fun refreshList(adapter: DetailViewRelationsAdapter) {
        if (adapter.visable == 0) {
            this.visibility = View.GONE
        } else {
            var Height = (adapter.visable - adapter.headers.size) * 56
            Height += adapter.headers.size * 48
            Height += (adapter.visable - 1)

            if (this.findViewById(R.id.ListView) != null)
                this.findViewById(R.id.ListView).layoutParams.height = Theme.convert(Height)
        }
    }

    /**
     * Change the header color.

     * @param color The resource id of the color
     */
    private fun setHeaderColor(color: Int) {
        val shape = Header.background as GradientDrawable
        shape.setColor(ContextCompat.getColor(appContext, color))
    }

    /**
     * Create an onClick event for listening for clicks.

     * @param view     The view id that will trigger the interface method
     * *
     * @param callback The activity that contains the interface method
     */
    fun setOnClickListener(view: Int, callback: onCardClickListener) {
        listener = callback
        this.findViewById(R.id.actionableIcon).visibility = View.VISIBLE
        Content!!.findViewById(view).setOnClickListener { v -> listener!!.onCardClicked(v.id) }
    }

    /**
     * Wraps the width of a card

     * @param width  The width of the image in dp
     * *
     * @param height The height of the image in dp
     */
    fun wrapImage(width: Int, height: Int) {
        setPadding(16)

        Header.layoutParams.width = Theme.convert(width + 32)
        Card.layoutParams.width = Theme.convert(width + 32)
        Card.layoutParams.height = Theme.convert(height + 92)

        if (Image == null)
            Image = findViewById(R.id.Image) as ImageView

        Image!!.layoutParams.height = Theme.convert(height)
        Image!!.layoutParams.width = Theme.convert(width)

        if (center)
            Header.gravity = Gravity.CENTER
    }

    /**
     * Set the card width.

     * @param amount   The amount of cards besides each other
     * *
     * @param maxWidth The maximum width in dp
     */
    fun setWidth(amount: Int?, maxWidth: Int?) {
        Card.layoutParams.width = getWidth(amount, maxWidth)
    }

    /**
     * Get the card width.

     * @param amount   The amount of cards besides each other
     * *
     * @param maxWidth The maximum width in dp
     * *
     * @return int The width that the card should be
     */
    private fun getWidth(amount: Int?, maxWidth: Int?): Int {
        var amount = amount
        var maxWidth = maxWidth
        if (amount === 0)
            amount = 1
        var divider = amount!! - 1
        divider = Theme.convert(divider * 4 + 16)
        val card = (getScreenWidth() - divider) / amount
        maxWidth = Theme.convert(maxWidth!!)

        if (card > maxWidth && maxWidth !== 0)
            return maxWidth
        else
            return card
    }

    /**
     * Get the screen width.

     * @return int The screen width in pixels
     */
    private fun getScreenWidth(): Int {
        if (screenWidth == 0) {
            try {
                screenWidth = Theme.convert(resources.configuration.screenWidthDp)
            } catch (e: Exception) {
                AppLog.logTaskCrash("Card", "getScreenWidth()", e)
            }

        }
        return screenWidth
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (Content != null) {
            Content.addView(child, index, params)
        } else {
            super.addView(child, index, params)
        }
    }

    /**
     * The Interface that will get triggered by the OnClick method.
     */
    interface onCardClickListener {
        fun onCardClicked(id: Int)
    }

    companion object {

        /**
         * Quickly init a card.

         * @param activity The activity
         * *
         * @param id The card ID
         * *
         * @param content The content layout
         */
        fun fastInit(activity: Activity, id: Int, content: Int) {
            (activity.findViewById(id) as Card).setContent(content)
        }

        /**
         * Quickly init a card.

         * @param view The View
         * *
         * @param id The card ID
         * *
         * @param content The content layout
         */
        fun fastInit(view: View, id: Int, content: Int) {
            (view.findViewById(id) as Card).setContent(content)
        }
    }
}