#ifndef ESP_I2C_H
#define ESP_I2C_H
static esp_err_t i2c_example_master_read_slave(i2c_port_t i2c_num, uint8_t* data_rd, size_t size);
static esp_err_t i2c_example_master_write_slave(i2c_port_t i2c_num, uint8_t* data_wr, size_t size)
#endif