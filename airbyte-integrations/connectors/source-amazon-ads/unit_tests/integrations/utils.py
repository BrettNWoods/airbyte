# Copyright (c) 2025 Airbyte, Inc., all rights reserved.

import operator
from typing import Any, Dict, List, Optional

from airbyte_cdk.models import AirbyteMessage, SyncMode
from airbyte_cdk.models import Level as LogLevel
from airbyte_cdk.test.catalog_builder import CatalogBuilder
from airbyte_cdk.test.entrypoint_wrapper import EntrypointOutput, read
from unit_tests.conftest import get_source


def read_stream(
    stream_name: str, sync_mode: SyncMode, config: Dict[str, Any], state: Optional[Dict[str, Any]] = None, expecting_exception: bool = False
) -> EntrypointOutput:
    catalog = CatalogBuilder().with_stream(stream_name, sync_mode).build()
    return read(get_source(config=config, state=state), config, catalog, state, expecting_exception)


def get_log_messages_by_log_level(logs: List[AirbyteMessage], log_level: LogLevel) -> List[str]:
    return map(operator.attrgetter("log.message"), filter(lambda x: x.log.level == log_level, logs))
