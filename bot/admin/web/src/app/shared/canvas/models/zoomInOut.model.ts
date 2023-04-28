export enum ZoomValue {
  IN = 'in',
  OUT = 'out'
}

export interface ActionZoomEvent {
  scale: number;
  clientX: number;
  clientY: number;
}

export const CANVAS_TRANSITION_TIMING = 300;
export const ZOOM_SPEED = 0.4;
export const ZOOM_SCALE = 0.75;
export const MAX_SCALE = 1;
export const MIN_SCALE = 0.2;
