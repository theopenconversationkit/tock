import { ScenarioSettings } from './settings.model';

export interface ScenarioSettingsState {
  loaded: boolean;
  settings: ScenarioSettings;
}
