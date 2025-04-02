package com.example.pharmatracker.features.medication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pharmatracker.R
import com.example.pharmatracker.core.data.model.Medication
import com.example.pharmatracker.core.utils.DateTimeUtils
import com.example.pharmatracker.databinding.ItemMedicationBinding
import java.util.*

class MedicationAdapter(
    private val onItemClick: (Medication) -> Unit
) : ListAdapter<Medication, MedicationAdapter.ViewHolder>(MedicationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMedicationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(medication: Medication) {
            binding.apply {
                medicationName.text = medication.name
                remainingQuantity.text = medication.quantity.toString()
                
                // Set medication form icon
                val iconRes = when (medication.form.toString().lowercase(Locale.getDefault())) {
                    "tablet" -> R.drawable.ic_pill
                    "capsule" -> R.drawable.ic_capsule
                    "syrup" -> R.drawable.ic_syrup
                    else -> R.drawable.ic_pill
                }
                medicationFormIcon.setImageResource(iconRes)

                // Set expiration date
                val daysUntilExpiry = DateTimeUtils.calculateDaysUntil(medication.expirationDate)
                val expiryText = if (medication.expirationDate != null) {
                    val formattedDate = DateTimeUtils.formatDate(medication.expirationDate)
                    root.context.getString(R.string.days_until_expiry, daysUntilExpiry, formattedDate)
                } else {
                    root.context.getString(R.string.no_expiry_date)
                }
                expirationDate.text = expiryText

                // Set color based on days until expiry
                val textColor = when {
                    medication.expirationDate == null -> R.color.text_secondary
                    daysUntilExpiry <= 7 -> R.color.error
                    daysUntilExpiry <= 30 -> R.color.warning
                    else -> R.color.text_primary
                }
                expirationDate.setTextColor(ContextCompat.getColor(root.context, textColor))

                // Set reminders count
                val reminderCount = medication.reminders?.size ?: 0
                remindersCount.text = root.context.resources.getQuantityString(
                    R.plurals.reminder_count,
                    reminderCount,
                    reminderCount
                )
            }
        }
    }

    private class MedicationDiffCallback : DiffUtil.ItemCallback<Medication>() {
        override fun areItemsTheSame(oldItem: Medication, newItem: Medication): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean {
            return oldItem == newItem
        }
    }
} 