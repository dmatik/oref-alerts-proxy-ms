# oref-alerts-proxy-ms

[![build]][3] [![maintenance]][2] [![last-commit]][2] <br>    
[![docker-version]][1] [![docker-pulls]][1] [![image-size]][1] <br>
[![quality-gate]][4]

Java Spring Boot MS to retrieve Israeli [Pikud Ha-Oref](https://www.oref.org.il/) so called "Red Color" alerts. <br/>
The project deployed on Docker Hub as [dmatik/oref-alerts](https://hub.docker.com/r/dmatik/oref-alerts).

<a href="https://www.buymeacoffee.com/bg7MaEJHc" target="_blank"><img height="41px" width="167px" src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee"></a>

## Usage
### Run from hub
#### docker run from hub
```text
docker run -d -p 49000:3000 --name oref-alerts dmatik/oref-alerts:latest
```

#### docker-compose from hub
```yaml
version: "3.6"
services:
    oref-alerts:
        image: dmatik/oref-alerts:latest
        container_name: oref-alerts
        hostname: oref-alerts
        network_mode: "bridge"
        ports:
          - 49000:3000
        restart: unless-stopped
```

### JSON Response Examples
#### Example for /current endpoint
```json
{
    "alert": "true",
    "current": {
        "data": [
            "סעד",
            "אשדוד - יא,יב,טו,יז,מרינה"
        ],
        "id": 1621242007417,
        "title": "התרעות פיקוד העורף"
    }
}
```
#### Example for /history endpoint
```json
{
    "history": [
        {
            "data": "בטחה",
            "date": "17.05.2021",
            "time": "13:31",
            "datetime": "2021-05-17T13:32:00"
        },
        {
            "data": "גילת",
            "date": "17.05.2021",
            "time": "13:31",
            "datetime": "2021-05-17T13:32:00"
        }
    ]
}
```

### Home-Assistant

#### Sensors
##### Fetch the current alert
```yaml
sensor:
  - platform: rest
    resource: http://[YOUR_IP]:49000/current
    name: redalert
    value_template: 'OK'
    json_attributes:
      - alert
      - current
    scan_interval: 5
    timeout: 30
```

##### Fetch the last day history alerts
> **_NOTE:_** This responce is very long, while there is 255 characters limit in HA sensors. <br/>
> Hence adding it to the attribute, which does not have such limit.
```yaml
sensor:
  - platform: rest
    resource: http://[YOUR_IP]:49000/history
    name: redalert_history
    value_template: 'OK'
    json_attributes:
      - "history"
    scan_interval: 120
    timeout: 30
```

#### Binary Sensors
##### Indicator for all alerts
```yaml
binary_sensor:
  - platform: template
    sensors:
      redalert_all:
        friendly_name: "Redalert All"
        value_template: >-
          {{ state_attr('sensor.redalert', 'alert') == "true" }}
```

##### Indicator for specific alert
```yaml
binary_sensor:
  - platform: template
    sensors:
      redalert_ashdod:
        friendly_name: "Redalert Ashdod"
        value_template: >-
          {{ state_attr('sensor.redalert', 'alert') == "true" and 
                    'אשדוד - יא,יב,טו,יז,מרינה' in state_attr('sensor.redalert', 'current')['data'] }}
```
<!-- Real Links -->
[1]: https://hub.docker.com/r/dmatik/oref-alerts
[2]: https://github.com/dmatik/oref-alerts-proxy-ms
[3]: https://github.com/dmatik/oref-alerts-proxy-ms/actions/workflows/build.yml
[4]: https://sonarcloud.io/dashboard/index/com.github.noraui:noraui
<!-- Badges Links -->
[maintenance]: https://img.shields.io/maintenance/yes/2021
[last-commit]: https://img.shields.io/github/last-commit/dmatik/oref-alerts-proxy-ms
[docker-pulls]: https://img.shields.io/docker/pulls/dmatik/oref-alerts?logo=docker
[docker-version]: https://img.shields.io/docker/v/dmatik/oref-alerts?logo=docker
[image-size]: https://img.shields.io/docker/image-size/dmatik/oref-alerts/latest?logo=docker
[build]: https://github.com/dmatik/oref-alerts-proxy-ms/actions/workflows/build.yml/badge.svg
[quality-gate]: https://sonarcloud.io/dashboard?id=dmatik_oref-alerts-proxy-ms