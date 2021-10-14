package co.tecno.sersoluciones.analityco

import androidx.lifecycle.ViewModel

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarViewModel @Inject constructor(
        private val repository: CreateCarRepository
) : ViewModel() {
}