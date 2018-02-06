import argparse
import json
import sys

from SplunkIntelOptimized import SplunkIntelOptimized
from sources.LogCorpus import LogCorpus
from sources.FileLoader import FileLoader

options = SplunkIntelOptimized.parse(['--sim_threshold=0.9'])


def test_log_ml_cluster_fail():
    control_start = 2
    test_start = 2
    print(options)
    prev_out_file = None
    while control_start <= 2 or test_start < 2:
        corpus = LogCorpus()

        corpus.load_prod_file_prev_run(
            'resources/logs/control_unknown_cluster_fail.json',
            [control_start, control_start],
            'resources/logs/test_unknown_cluster_fail.json',
            [test_start, test_start], prev_out_file)

        splunk_intel = SplunkIntelOptimized(corpus, options)
        corpus = splunk_intel.run()
        assert len(corpus.anom_clusters) == 2

        control_start = control_start + 1
        test_start = test_start + 1


# Uses the analysis output to setup a test case
def test_log_ml_out_1():
    data = FileLoader.load_data('resources/logs/log_ml_out_1.json')
    control = data['control_events']
    test = data['test_events']
    unknown = data['unknown_events']
    corpus = LogCorpus()
    for events in control.values():
        for event in events:
            corpus.add_event(event, 'control_prev')

    for events in test.values():
        for event in events:
            corpus.add_event(event, 'test_prev')

    sio = SplunkIntelOptimized(corpus, SplunkIntelOptimized.parse(['--sim_threshold', '0.9']))
    result = sio.run()
    assert len(result.anom_clusters) == 2


# Uses the analysis output to setup a test case
def test_log_ml_out_2():
    data = FileLoader.load_data('resources/logs/log_ml_out_2.json')
    control = data['control_events']
    test = data['test_events']
    unknown = data['unknown_events']
    corpus = LogCorpus()
    for events in control.values():
        for event in events:
            corpus.add_event(event, 'control_prev')

    for events in test.values():
        for event in events:
            corpus.add_event(event, 'test_prev')

    sio = SplunkIntelOptimized(corpus, SplunkIntelOptimized.parse(['--sim_threshold', '0.9']))
    result = sio.run()
    assert len(result.anom_clusters) == 3
    assert result.anom_clusters[0].values()[0]['cluster_label'] == 0
    assert result.anom_clusters[1].values()[0]['cluster_label'] == 1
    assert result.anom_clusters[2].values()[0]['cluster_label'] == 2


def main(args):
    test_log_ml_out_2()


if __name__ == "__main__":
    main(sys.argv)
