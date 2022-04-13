using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BossAreaFlag : MonoBehaviour
{
    void OnCollisionEnter2D(Collision2D collision) { 
        if(collision.gameObject.name == "PlayerShipLite" || collision.gameObject.name == "PlayerShip"){
			Destroy(gameObject);
		}
    }
}
