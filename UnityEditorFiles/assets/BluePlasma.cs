using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BluePlasma : MonoBehaviour
{
    private Rigidbody2D Rigid;
	private Vector2 AimDirection = Vector2.left;
	private float T = 0.0f;
	private float WaitT = 8.0f;
	
	
    // Awake is called when the object is instantiated
    private void TheStart(Vector2 AD)
    {
        Rigid = GetComponent<Rigidbody2D>();
		AimDirection = AD;
    }
	
    //public void Aim(Vector2 AD){
		//AimDirection = AD;
	//}
    // Update is called once per frame
    void Update()
    {
		T += Time.deltaTime;
		if(T > WaitT){
			Destroy(gameObject);
		}
        Rigid.MovePosition(Rigid.position + (AimDirection * (8)) * Time.fixedDeltaTime);
		
    }
	private void OnTriggerEnter2D(Collider2D collision) { 
        
			Destroy(gameObject);
		
    }
}
