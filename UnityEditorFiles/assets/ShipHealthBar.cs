using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ShipHealthBar : MonoBehaviour
{
   
   public Slider slider;
   
	public void changeHealth(float H){
		slider.value = H;
	}
	
	
}
