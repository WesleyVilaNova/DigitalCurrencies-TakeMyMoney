package com.example.take_my_money.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.take_my_money.R
import com.example.take_my_money.databinding.DetailsActivityBinding
import com.example.take_my_money.ui.data.dao.ICoinDAO
import com.example.take_my_money.ui.data.database.CoinDataBase
import com.example.take_my_money.ui.data.entity.CoinEntity
import com.example.take_my_money.ui.interfaces.IWebService
import com.example.take_my_money.ui.repository.RepositoryCoinsDetails
import com.example.take_my_money.ui.repository.RepositoryDataSource
import com.example.take_my_money.ui.utils.Constants
import com.example.take_my_money.ui.view.coindetails.CoinDetailsViewModel
import com.example.take_my_money.ui.view.coindetails.CoinDetailsViewModelFactory
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: DetailsActivityBinding
    private lateinit var viewModel: CoinDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = DetailsActivityBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val coinIDAO: ICoinDAO =
            CoinDataBase.getInstance(this).iCoinDAO
        val repositoryCoinsDetails = RepositoryCoinsDetails(IWebService.retrofit)
        viewModel = ViewModelProvider(
            this,
            CoinDetailsViewModelFactory(RepositoryDataSource(coinIDAO), repositoryCoinsDetails)
        )[CoinDetailsViewModel::class.java]

        getCoinFromDetailsScreen()
    }

    private fun getCoinFromDetailsScreen() {
        val nameCoin = intent.getStringExtra(Constants.KEY_INTENT)
        requestApiDetails(nameCoin.toString())
    }

    private fun requestApiDetails(nameCoin: String?) {
        nameCoin?.let { viewModel.getDetailsApiCoin(it) }
        passDataToScreen()
    }

    private fun passDataToScreen() {
        viewModel.coinDetail.observe(this) { coinDetails ->
            coinDetails?.let {
                binding.txCoin.text = coinDetails.name
                binding.txValue.text = NumberFormat.getInstance().format(coinDetails.price_usd)
                binding.txValueHour.text = NumberFormat.getInstance().format(coinDetails.volume_1hrs_usd)
                binding.txValueDay.text = NumberFormat.getInstance().format(coinDetails.volume_1day_usd)
                binding.txValueMonth.text = NumberFormat.getInstance().format(coinDetails.volume_1mth_usd)
                Picasso.get().load(coinDetails.getPathUrlImage()).into(binding.imView)
                loadDataBase(coinDetails)
            }
        }
        viewModel.messageError.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }

    private fun loadDataBase(coinDetails: CoinEntity) = CoroutineScope(Dispatchers.IO).launch {
        viewModel.allCoins(coinDetails)
        insertedFavorites()
    }

    private fun insertedFavorites() {
        binding.btnAddRemove.setOnClickListener {
            if (binding.btnAddRemove.text == getString(R.string.add)) {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.insertCoinDetailsDataBase()
                    binding.btnAddRemove.text = getString(R.string.remover)
                    telaFavorite()
                }
            } else {
                deletedFavorite()
            }
        }
    }

    private fun telaFavorite() {
        val intent = Intent(this, FavoriteActivity::class.java)
        startActivity(intent)
    }

    private fun deletedFavorite() {
        binding.btnAddRemove.setOnClickListener {
            if (binding.btnAddRemove.text == getString(R.string.remover)) {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.deleteCoin()
                    binding.btnAddRemove.text = getString(R.string.add)
                    insertedFavorites()
                }
            }
        }
    }
}
