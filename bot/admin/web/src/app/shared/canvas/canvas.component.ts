import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';

import {
  CanvaAction,
  ActionZoomEvent,
  CANVAS_TRANSITION_TIMING,
  MAX_SCALE,
  MIN_SCALE,
  OffsetPosition,
  Position,
  ZoomValue,
  ZOOM_SCALE,
  ZOOM_SPEED
} from './models';

@Component({
  // eslint-disable-next-line
  selector: '[tock-canvas]',
  templateUrl: './canvas.component.html',
  encapsulation: ViewEncapsulation.None
})
export class CanvasComponent implements OnInit, OnChanges, AfterViewInit {
  @Input() position: OffsetPosition;
  @Input() centerCanvasAtInitialisation: boolean = true;
  @Input() centerCanvasX: boolean = true;
  @Input() centerCanvasY: boolean = false;
  @Input() isFullscreen: boolean = false;
  @Input() maxScale: number = MAX_SCALE;
  @Input() hideActions: Array<CanvaAction> = [];

  @Output() onFullscreen = new EventEmitter<boolean>();

  private canvasPos = { x: 0, y: 0 };
  private canvasPosOffset = { x: 0, y: 0 };
  private pointer = { x: 0, y: 0 };
  private canvasScale: number = 1;
  private isDragingCanvas: Position;

  private wrapper: HTMLElement;
  canvas: HTMLElement;

  constructor(private canvasWrapperElement: ElementRef) {}

  ngOnInit(): void {
    this.wrapper = this.canvasWrapperElement.nativeElement;
    this.wrapper.style.position = 'relative';
    this.wrapper.style.cursor = 'move';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.canvas && changes.position?.currentValue && changes.position.currentValue !== changes.position.previousValue) {
      this.centerOnElement(changes.position.currentValue);
    }
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.wrapper.children.length > 1) this.canvas = this.wrapper.querySelector('#canvas');
      else this.canvas = this.wrapper.firstElementChild as HTMLElement;

      if (this.canvas) {
        if (this.centerCanvasAtInitialisation) this.centerCanvas();
      } else {
        throw new Error('The container contains several elements. It is necessary to give a "canvas" identifier to the canvas element');
      }
    });
  }

  private setCanvasTransform(): void {
    this.canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  zoomCanvas(wheelEvent?: WheelEvent, actionEvent?: ActionZoomEvent): void {
    this.throwErrorUndefinedCanvas();
    if (!wheelEvent && !actionEvent) throw new Error('No event defined');

    // Get mouse offset
    this.pointer.x = (wheelEvent?.clientX || actionEvent.clientX) - this.wrapper.offsetLeft;
    this.pointer.y = (wheelEvent?.clientY || actionEvent.clientY) - this.wrapper.offsetTop;

    // get the current position of the canvas at the current scale relative to the pointer position
    this.canvasPosOffset.x = (this.pointer.x - this.canvasPos.x) / this.canvasScale;
    this.canvasPosOffset.y = (this.pointer.y - this.canvasPos.y) / this.canvasScale;

    // set new scale
    const scale = wheelEvent ? wheelEvent.deltaY : actionEvent.scale;
    this.canvasScale += -1 * Math.max(-1, Math.min(1, scale)) * ZOOM_SPEED * this.canvasScale;
    this.canvasScale = Math.max(MIN_SCALE, Math.min(this.maxScale, this.canvasScale));

    const zoomOneMagnetism = 0.2;
    if (this.canvasScale >= 1 - zoomOneMagnetism && this.canvasScale <= 1 + zoomOneMagnetism) this.canvasScale = 1;

    // set new position of the canvas
    this.canvasPos.x = -this.canvasPosOffset.x * this.canvasScale + this.pointer.x;
    this.canvasPos.y = -this.canvasPosOffset.y * this.canvasScale + this.pointer.y;

    this.setCanvasTransform();
  }

  fullscreen(open: boolean): void {
    if (open) {
      this.onFullscreen.emit(true);
    } else {
      this.onFullscreen.emit(false);
    }
  }

  zoom(v: ZoomValue): void {
    const actionEvent: ActionZoomEvent = {
      scale: v === ZoomValue.OUT ? ZOOM_SCALE : -ZOOM_SCALE,
      clientX: this.wrapper.offsetWidth / 2 + this.wrapper.offsetLeft,
      clientY: this.wrapper.offsetHeight / 2 + this.wrapper.offsetTop
    };
    this.zoomCanvas(null, actionEvent);
  }

  centerCanvas(): void {
    this.throwErrorUndefinedCanvas();

    if (this.centerCanvasX) {
      this.canvasPos.x = ((this.canvas.offsetWidth * this.canvasScale - this.wrapper.offsetWidth) / 2) * -1;
    }
    if (this.centerCanvasY && this.canvas.offsetHeight * this.canvasScale < this.wrapper.offsetHeight) {
      this.canvasPos.y = ((this.canvas.offsetHeight * this.canvasScale - this.wrapper.offsetHeight) / 2) * -1;
    }

    this.setCanvasTransform();
  }

  centerOnElement(position: OffsetPosition): void {
    this.throwErrorUndefinedCanvas();

    this.canvasPos.x = position.offsetLeft * this.canvasScale * -1 + (this.wrapper.offsetWidth - position.offsetWidth) / 2;
    this.canvasPos.y = position.offsetTop * this.canvasScale * -1 + (this.wrapper.offsetHeight - position.offsetHeight) / 2;

    this.setCanvasTransform();
  }

  throwErrorUndefinedCanvas(): void {
    if (!this.canvas) throw new Error('The canvas is not defined');
  }

  resetPosition() {
    this.canvasScale = 1;

    if (this.position) {
      this.centerOnElement(this.position);
    } else {
      this.throwErrorUndefinedCanvas();

      if (this.centerCanvasX && this.centerCanvasAtInitialisation) {
        this.canvasPos.x = ((this.canvas.offsetWidth * this.canvasScale - this.wrapper.offsetWidth) / 2) * -1;
      } else {
        this.canvasPos.x = 0;
      }

      if (this.centerCanvasY) {
        if (this.canvas.offsetHeight * this.canvasScale < this.wrapper.offsetHeight) {
          this.canvasPos.y = ((this.canvas.offsetHeight * this.canvasScale - this.wrapper.offsetHeight) / 2) * -1;
        }
      } else {
        this.canvasPos.y = 0;
      }

      this.setCanvasTransform();
    }
  }

  @HostListener('wheel', ['$event'])
  public onWheel(event: WheelEvent) {
    event.preventDefault();
    this.throwErrorUndefinedCanvas();

    this.zoomCanvas(event);
  }

  /**
   * Takes the new positions of the canvas and the client
   * @param {MouseEvent} event
   */
  @HostListener('mousedown', ['$event'])
  onMouseDownCanvas(event: MouseEvent): void {
    this.throwErrorUndefinedCanvas();
    event.preventDefault();

    if (event.button == 0) {
      this.isDragingCanvas = {
        left: this.canvasPos.x,
        top: this.canvasPos.y,
        clientX: event.clientX,
        clientY: event.clientY
      };
      let canvas = this.canvas;
      canvas.style.transition = 'unset';
    }
  }

  /**
   * Stop the slide of the canvas
   * @param {MouseEvent} event
   */
  @HostListener('mouseup', ['$event'])
  onMouseUpCanvas(event: MouseEvent): void {
    this.throwErrorUndefinedCanvas();
    event.preventDefault();

    if (event.button == 0) {
      this.isDragingCanvas = undefined;
      let canvas = this.canvas;
      canvas.style.transition = `transform .${CANVAS_TRANSITION_TIMING}s`;
    }
  }

  /**
   * Change the position of the canvas in the container
   * @param {MouseEvent} event
   */
  @HostListener('mousemove', ['$event'])
  onMouseMoveCanvas(event: MouseEvent): void {
    if (this.canvas && this.isDragingCanvas && event.button == 0) {
      const dx = event.clientX - this.isDragingCanvas.clientX;
      const dy = event.clientY - this.isDragingCanvas.clientY;
      this.canvasPos.x = this.isDragingCanvas.left + dx;
      this.canvasPos.y = this.isDragingCanvas.top + dy;

      this.setCanvasTransform();
    }
  }
}
