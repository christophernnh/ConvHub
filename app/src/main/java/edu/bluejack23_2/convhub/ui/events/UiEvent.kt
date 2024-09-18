package edu.bluejack23_2.convhub.ui.events

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}