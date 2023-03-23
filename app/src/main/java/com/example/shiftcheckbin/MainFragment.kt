package com.example.shiftcheckbin

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.shiftcheckbin.databinding.EnterScreenBinding
import com.example.shiftcheckbin.network.models.ApiResponse
import com.example.shiftcheckbin.network.viewmodels.MainFrameViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*


class MainFragment : Fragment() {
    private val viewModel by lazy { ViewModelProvider(this)[MainFrameViewModel::class.java] }
    private lateinit var binding: EnterScreenBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    private lateinit var adapter: HistoryAdapter

    private lateinit var currentNumber: String
    private var longitude = -1f
    private var latitude = -1f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = EnterScreenBinding.inflate(inflater, container, false)

        sharedPreferences =
            requireContext().getSharedPreferences("shared preferences", MODE_PRIVATE)


        val json = sharedPreferences.getString("numberHistory", null)

        val type: Type = object : TypeToken<ArrayList<String>?>() {}.type
        val data: ArrayList<String>? = gson.fromJson(json, type)
        if (data == null) {
            viewModel.history = ArrayList()
        } else {
            viewModel.history = data
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val doneClickListener =
            TextView.OnEditorActionListener { text, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    text.clearFocus()
                    currentNumber = binding.inputText.rawText.toString()
                    viewModel.getInfo(currentNumber)
                }
                false
            }

        binding.inputText.setOnEditorActionListener(doneClickListener)

        setUpRequestIntentsToAnotherApps()

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = HistoryAdapter(viewModel.history)

        binding.recyclerView.adapter = adapter

        binding.clearButton.setOnClickListener {
            viewModel.history.clear()
            adapter.notifyDataSetChanged()
        }

        viewModel.cardInfo.observe(viewLifecycleOwner) {
            when (it) {
                ApiResponse.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.dataLayout.visibility = View.INVISIBLE
                    binding.serverErrorResponse.visibility = View.INVISIBLE
                }
                is ApiResponse.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.dataLayout.visibility = View.INVISIBLE
                    binding.serverErrorResponse.visibility = View.VISIBLE
                    when (it.code) {
                        404 -> binding.serverErrorResponse.text =
                            getString(R.string.bin_number_not_found)
                        429 -> binding.serverErrorResponse.text =
                            getString(R.string.too_many_requests)
                        408 -> binding.serverErrorResponse.text =
                            getString(R.string.time_out_error)
                        else -> binding.serverErrorResponse.text =
                            getText(R.string.unexpected_error)
                    }
                }
                is ApiResponse.Success -> {
                    viewModel.saveNumber(currentNumber, adapter)
                    binding.progressBar.visibility = View.GONE
                    binding.dataLayout.visibility = View.VISIBLE
                    binding.serverErrorResponse.visibility = View.INVISIBLE
                    it.data.let { data ->
                        latitude = data.country.latitude
                        longitude = data.country.longitude

                        binding.brandText.text = data.brand
                        binding.length.text = data.number.length.toString()

                        binding.country.text = String.format(
                            getString(R.string.country_text),
                            data.country.emoji,
                            data.country.name
                        )

                        binding.bankName.text =
                            String.format(getString(R.string.bank_name), data.bank.name)
                        binding.bankTown.text = data.bank.city
                        binding.bankUrl.text = data.bank.url
                        binding.bankPhone.text = data.bank.phone


                        setRightImageOrPasteTextForCardScheme(data.scheme)

                        highlightTheCorrectText(
                            data.type,
                            binding.debitCard,
                            binding.creditCard,
                            "debit",
                            "credit"
                        )

                        highlightTheCorrectText(
                            data.prepaid,
                            binding.prepaidYes,
                            binding.prepaidNo,
                            true,
                            false
                        )

                        highlightTheCorrectText(
                            data.number.luhn,
                            binding.luhnYes,
                            binding.luhnNo,
                            true,
                            false
                        )
                    }
                }
            }
        }
    }

    private fun setUpRequestIntentsToAnotherApps() {
        binding.bankUrl.setOnClickListener {
            var url = binding.bankUrl.text.toString()
            if (!(url.startsWith("http://") || url.startsWith("https://"))) url = "http://$url";

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        binding.bankPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${(it as TextView).text}")
            startActivity(intent)
        }

        binding.country.setOnClickListener {
            val uri: String = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }
    }

    private fun setRightImageOrPasteTextForCardScheme(scheme: String) {
        when (scheme) {
            "mastercard" -> {
                binding.networkImage.setImageResource(R.drawable.mastercard)
                binding.networkImage.visibility = View.VISIBLE
                binding.networkType.visibility = View.INVISIBLE
            }
            "visa" -> {
                binding.networkImage.setImageResource(R.drawable.visa)
                binding.networkImage.visibility = View.VISIBLE
                binding.networkType.visibility = View.INVISIBLE
            }
            else -> {
                binding.networkType.visibility = View.VISIBLE
                binding.networkType.text = scheme
                binding.networkImage.visibility = View.INVISIBLE
            }
        }
    }

    //Generic because data may be string(debit/credit card) or boolean(prepaid and luhn properties)
    private fun <T> highlightTheCorrectText(
        data: T?,
        textViewForYes: TextView,
        textViewForNo: TextView,
        conditionForTheFirst: T,
        conditionForTheSecond: T,
    ) {
        when (data) {
            conditionForTheFirst -> {
                textViewForYes.typeface = Typeface.DEFAULT_BOLD
                textViewForNo.typeface = Typeface.DEFAULT
            }
            conditionForTheSecond -> {
                textViewForYes.typeface = Typeface.DEFAULT
                textViewForNo.typeface = Typeface.DEFAULT_BOLD
            }
            else -> {
                textViewForYes.typeface = Typeface.DEFAULT
                textViewForNo.typeface = Typeface.DEFAULT
            }
        }
    }

    // For some reason it works unstable (the fact of underlining depends on the size of the text). Didn't use
    private fun getUnderlinedString(inputString: String?): SpannableString {
        if (inputString != null) {
            val modifiedString = SpannableString(inputString)
            modifiedString.setSpan(UnderlineSpan(), 0, modifiedString.length, 0)
            return modifiedString
        }

        return SpannableString("")
    }

    override fun onStop() {
        super.onStop()
        val editor = sharedPreferences.edit()

        val json: String = gson.toJson(viewModel.history)
        editor.putString("numberHistory", json)
        editor.apply()
    }

    inner class HistoryAdapter(var history: ArrayList<String>) :
        RecyclerView.Adapter<HistoryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
            val view = layoutInflater.inflate(R.layout.history_item, parent, false)
            return HistoryHolder(view)
        }

        override fun getItemCount(): Int {
            return history.size
        }

        override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
            val historyItem = history[position]
            holder.bind(historyItem)
        }

    }

    inner class HistoryHolder(itemView: View) : ViewHolder(itemView), View.OnClickListener {
        private lateinit var data: String
        private val text: TextView = itemView.findViewById(R.id.itemText)
        fun bind(number: String) {
            data = number
            text.text = data
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            binding.inputText.setText(data)
            binding.inputText.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }
    }
}