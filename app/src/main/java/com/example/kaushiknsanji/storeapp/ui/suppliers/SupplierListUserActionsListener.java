/*
 * Copyright 2018 Kaushik N. Sanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kaushiknsanji.storeapp.ui.suppliers;

import com.example.kaushiknsanji.storeapp.data.local.models.SupplierLite;

public interface SupplierListUserActionsListener {

    void onEditSupplier(final int itemPosition, SupplierLite supplier);

    void onDeleteSupplier(final int itemPosition, SupplierLite supplier);

    void onDefaultPhoneClicked(final int itemPosition, SupplierLite supplier);

    void onDefaultEmailClicked(final int itemPosition, SupplierLite supplier);
}
