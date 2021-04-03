package com.example.photoweather.view.fragments

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoweather.R
import com.example.photoweather.adapter.ImagesAdapter
import com.example.photoweather.databinding.FragmentHomeBinding
import com.example.photoweather.utils.AppUtils
import com.example.photoweather.utils.DialogDismiss
import com.example.photoweather.utils.RecyclerViewListener
import com.example.photoweather.viewModel.BaseViewModel
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeFragment : Fragment(), RecyclerViewListener, DialogDismiss {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: BaseViewModel
    private lateinit var adapter: ImagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        configurationChanged()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.statusCheck(requireActivity())
    }

    private fun configurationChanged() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.imagesRv.layoutManager = LinearLayoutManager(requireContext())
        } else {
            binding.imagesRv.layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[BaseViewModel::class.java]
        init()
        viewModel.imagesListLiveData.observe(
            requireActivity(),
            { list -> adapter.updateDate(list) })
    }

    private fun setAdapterOnChangeListener() {
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                val itemCount: Int = adapter.itemCount
                if (itemCount == 0) {
                    binding.homeAnimator.displayedChild = 0
                } else binding.homeAnimator.displayedChild = 1
            }
        })
    }

    private fun init() {
        binding.homeToolbar.tvToolbarTitle.text = resources.getString(R.string.home)
        adapter = ImagesAdapter(requireContext(), arrayListOf(), this)
        binding.imagesRv.adapter = adapter
        setAdapterOnChangeListener()
        setFabBehavior()
        binding.takeImageFab.setOnClickListener {
            if (AppUtils.checkPermissions(requireActivity(), 0)) {
                requireActivity().findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.from_home_to_create_image)
            }
        }
    }

    private fun setFabBehavior() {
        binding.imagesRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val b = dy <= 0
                if (b) binding.takeImageFab.extend()
                else binding.takeImageFab.shrink()
            }
        })
    }

    override fun onItemClicked(position: Int, viewId: Int) {
        viewModel.selectedImageURL.value = viewModel.imagesList.value!![position]
        when (viewId) {
            R.id.img -> {
                val displayDlg = DisplayImageDialog()
                displayDlg.show(requireActivity().supportFragmentManager, displayDlg.tag)
            }
            R.id.delete_img -> {
                // SHOW CONFIRMATION DIALOG
                MainScope().launch {
                    val dialog = ProgressDialog()
                    dialog.isCancelable = false
                    dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                    viewModel.deleteImage(viewModel.selectedImageURL.value)
                    delay(1000)
                    viewModel.getAllImages()
                    dialog.dismiss()
                    AppUtils.makeToast(
                        requireContext(),
                        resources.getString(R.string.deleted_successfuly)
                    )
                }
            }
            R.id.share_img -> {
                val shareDialog = ShareOptionsDialog()
                shareDialog.setListener(this)
                shareDialog.show(requireActivity().supportFragmentManager, shareDialog.tag)
            }
        }
    }

    override fun onDialogDismiss(viewID: Int) {
        when (viewID) {
            R.id.share_via_facebook -> {
                val image = BitmapFactory.decodeFile(viewModel.selectedImageURL.value)
                val sharePhoto = SharePhoto.Builder()
                    .setBitmap(image)
                    .build();

                val shareContent = SharePhotoContent.Builder()
                    .addPhoto(sharePhoto)
                    .build()
                val fbShareDialog = ShareDialog(requireActivity())
                fbShareDialog.show(shareContent)
            }
            R.id.share_via_twitter -> {
                AppUtils.makeToast(requireContext() , "sorry , this feature hasn't implemented yet")
            }
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}