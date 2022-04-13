using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class HyperBeam : MonoBehaviour
{
    private float T = 0.0f;
	private float WaitT = 0.2f;

    // Update is called once per frame
    void Update()
    {
        T = T + Time.deltaTime;
		
		if(T > WaitT){
			Destroy(gameObject);
		}
    }
}
