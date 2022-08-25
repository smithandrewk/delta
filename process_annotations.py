import pandas as pd
import json
import sys


if len(sys.argv) != 3:
    print("Usage: python3 process_annotations.py <filename> <label-studio file url>")
    print(sys.argv)
    exit(1)

raw_file = sys.argv[1]
file_url = sys.argv[2]

df = pd.read_csv(raw_file, header=None)

current_activity = ""
annotations = [{'Activity': df.loc[0][9], 'Start': df.loc[0][10]}]

for i,activity in enumerate(df[9]):
    if activity != current_activity:
            annotations[-1]['Stop'] = df.loc[i][10]
            annotations.append({'Activity': activity, 'Start': df.loc[i][10]})
    current_activity = activity



annotations_json = {
    "data": {
        "timeseriesUrl": file_url
    },

    "predictions": [
        {
            "result": []
        }
    ]
}

for i,annotation in enumerate(annotations):
    if annotation['Activity'] != "NONE":
        annotations_json["predictions"][0]["result"].append(
            {
                "value": {
                "start": str(annotation["Start"]),
                "end": str(annotation["Stop"]),
                "instant": False,
                "timeserieslabels": [
                    annotation['Activity']
                ]
                },
                "id": str(i),
                "from_name": "label",
                "to_name": "ts",
                "type": "timeserieslabels",
                "origin": "manual"
            }
        )

json_filename = f"annotations-{raw_file.replace('.csv', '.json').replace('/', '-')}"
with open(json_filename, 'w+') as f:
    json.dump(annotations_json, f)