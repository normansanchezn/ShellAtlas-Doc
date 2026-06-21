package com.shelldocs.app

import com.shelldocs.core.common.persistence.NoOpSessionPreferences as CoreNoOpSessionPreferences
import com.shelldocs.core.common.persistence.SessionPreferences as CoreSessionPreferences

typealias SessionPreferences = CoreSessionPreferences

object NoOpSessionPreferences : SessionPreferences by CoreNoOpSessionPreferences
