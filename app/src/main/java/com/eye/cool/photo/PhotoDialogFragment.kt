package com.eye.cool.photo

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eye.cool.permission.Rationale
import com.eye.cool.photo.params.DialogParams
import com.eye.cool.photo.params.ImageParams
import com.eye.cool.photo.params.Params
import com.eye.cool.photo.support.OnActionListener
import com.eye.cool.photo.support.OnClickListener
import com.eye.cool.photo.support.OnSelectListenerWrapper
import com.eye.cool.photo.utils.PhotoExecutor
import com.eye.cool.photo.view.DefaultView

/**
 * Created by cool on 18-3-9
 */
class PhotoDialogFragment : AppCompatDialogFragment() {

  private lateinit var executor: PhotoExecutor
  private lateinit var params: Params
  private var createByBuilder = false

  override fun onAttach(context: Context?) {
    if (!createByBuilder) throw IllegalStateException("You must create it by Builder.build()!")
    super.onAttach(context)
    executor = PhotoExecutor(params)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    var view: View? = params.dialogParams.contentView
    if (view == null) {
      view = DefaultView(context)
      view.setActionListener(executor)
    } else {
      val method = view.javaClass.getDeclaredMethod("setActionListener", OnActionListener::class.java)
          ?: throw IllegalArgumentException("Custom View must declare method setActionListener(OnActionListener)")
      method.invoke(view, executor)
    }
    return view
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialogParams = params.dialogParams
    val dialog = AppCompatDialog(context, dialogParams.dialogStyle)
    val window = dialog.window ?: return dialog
    window.setWindowAnimations(dialogParams.animStyle)
    val layoutParams = window.attributes
    layoutParams.x = dialogParams.xPos
    layoutParams.y = dialogParams.yPos
    dialog.onWindowAttributesChanged(layoutParams)
    dialog.setOnShowListener(dialogParams.onShowListener)
    dialog.setOnDismissListener(dialogParams.onDismissListener)
    dialog.setOnCancelListener(dialogParams.onCancelListener)
    return dialog
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    executor.setOnClickListener(object : OnClickListener {
      override fun onClick(which: Int) {
        view?.visibility = View.GONE
        params.dialogParams.onClickListener?.onClick(which)
      }
    })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    super.onActivityResult(requestCode, resultCode, intent)
    if (resultCode == Activity.RESULT_OK) {
      executor.onActivityResult(requestCode, intent)
    } else if (resultCode == Activity.RESULT_CANCELED) {
      dismissAllowingStateLoss()
    }
  }

  fun show(manager: FragmentManager) {
    show(manager, javaClass.simpleName)
  }

  fun show(activity: AppCompatActivity) {
    show(activity.supportFragmentManager, javaClass.simpleName)
  }

  fun show(fragment: Fragment) {
    show(fragment.childFragmentManager, javaClass.simpleName)
  }

  class Builder {
    private val dialog = PhotoDialogFragment()
    private val paramsBuilder = Params.Builder(dialog)

    /**
     * The params for shown dialog
     * @param dialogParams
     */
    fun setDialogParams(dialogParams: DialogParams): Builder {
      paramsBuilder.setDialogParams(dialogParams)
      return this
    }

    /**
     * The params for selected image
     * @param imageParams
     */
    fun setImageParams(imageParams: ImageParams): Builder {
      paramsBuilder.setImageParams(imageParams)
      return this
    }

    /**
     * Permission setRationale when need
     * @param rationale
     */
    fun setRationale(rationale: Rationale): Builder {
      paramsBuilder.setRationale(rationale)
      return this
    }

    /**
     * Permission setting's setRationale when need
     * @param rationale
     */
    fun setRationaleSetting(rationale: Rationale): Builder {
      paramsBuilder.setRationaleSetting(rationale)
      return this
    }

    fun build(): PhotoDialogFragment {
      val params = paramsBuilder.build()
      val wrapper = OnSelectListenerWrapper(dialog, params.imageParams.onSelectListener)
      params.imageParams.onSelectListener = wrapper
      dialog.params = params
      dialog.createByBuilder = true
      return dialog
    }
  }
}