/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package johnrobert.delinila.jesusreigns.barcodedetection;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import johnrobert.delinila.jesusreigns.R;
import johnrobert.delinila.jesusreigns.camera.WorkflowModel;
import johnrobert.delinila.jesusreigns.camera.WorkflowModel.WorkflowState;

/** Displays the bottom sheet to present barcode fields contained in the detected barcode. */
public class BarcodeResultFragment extends BottomSheetDialogFragment {

  private static final String TAG = "BarcodeResultFragment";
  private static final String ERROR = "error";

  public static void show(FragmentManager fragmentManager, String barcode, String error) {
    BarcodeResultFragment barcodeResultFragment = new BarcodeResultFragment();
    Bundle bundle = new Bundle();
    barcodeResultFragment.show(fragmentManager, TAG);
  }

  public static void dismiss(FragmentManager fragmentManager) {
    BarcodeResultFragment barcodeResultFragment =
        (BarcodeResultFragment) fragmentManager.findFragmentByTag(TAG);
    if (barcodeResultFragment != null) {
      barcodeResultFragment.dismiss();
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.barcode_bottom_sheet, viewGroup);
    return view;
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialogInterface) {
    if (getActivity() != null) {
      // Back to working state after the bottom sheet is dismissed.
      ViewModelProviders.of(getActivity())
          .get(WorkflowModel.class)
          .setWorkflowState(WorkflowState.DETECTING);
    }
    super.onDismiss(dialogInterface);
  }
}
